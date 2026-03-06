#!/bin/bash
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#  Kafka Setup Script for EC2 t2.micro (AWS Free Tier)
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
#
#  Installs Apache Kafka in KRaft mode (no Zookeeper) on the same EC2
#  instance that runs the MedicalStore app. KRaft mode saves ~300MB RAM
#  by eliminating the Zookeeper process.
#
#  Resource Budget (t2.micro = 1 vCPU, 1 GB RAM):
#    - Kafka (KRaft):    ~256 MB  (KAFKA_HEAP_OPTS=-Xmx256m)
#    - Spring Boot App:  ~384 MB  (JAVA_OPTS=-Xmx384m)
#    - OS overhead:      ~200 MB
#    - Total:            ~840 MB  ✅ fits in 1 GB
#
#  Usage:
#    sudo bash scripts/setup-kafka-ec2.sh
#
#  This script is idempotent — safe to run multiple times.
# ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

set -euo pipefail

KAFKA_VERSION="3.7.0"
SCALA_VERSION="2.13"
KAFKA_DIR="/opt/kafka"
KAFKA_DATA_DIR="/opt/kafka/kraft-logs"
KAFKA_USER="kafka"

echo "═══════════════════════════════════════════════════════════"
echo "  Installing Apache Kafka ${KAFKA_VERSION} (KRaft mode)"
echo "  Target: EC2 t2.micro — AWS Free Tier"
echo "═══════════════════════════════════════════════════════════"

# ─── 0. Create swap file (safety buffer for 1 GB RAM) ────────────────────
if [ ! -f /swapfile ]; then
    echo "[0/6] Creating 512 MB swap file..."
    sudo dd if=/dev/zero of=/swapfile bs=1M count=512 status=progress
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab > /dev/null
    echo "  Swap enabled: 512 MB"
else
    echo "[0/6] Swap file already exists"
fi

# ─── 1. Install Java if not present ──────────────────────────────────────
if ! command -v java &> /dev/null; then
    echo "[1/6] Installing Java 17..."
    sudo yum install -y java-17-amazon-corretto-headless 2>/dev/null || \
    sudo apt-get install -y openjdk-17-jre-headless 2>/dev/null || \
    echo "Java already installed or unsupported package manager"
else
    echo "[1/6] Java already installed: $(java -version 2>&1 | head -1)"
fi

# ─── 2. Create kafka user ────────────────────────────────────────────────
if ! id "${KAFKA_USER}" &> /dev/null; then
    echo "[2/6] Creating kafka system user..."
    sudo useradd -r -s /sbin/nologin ${KAFKA_USER}
else
    echo "[2/6] Kafka user already exists"
fi

# ─── 3. Download and extract Kafka ───────────────────────────────────────
if [ ! -d "${KAFKA_DIR}/bin" ]; then
    echo "[3/6] Downloading Kafka ${KAFKA_VERSION}..."
    cd /tmp
    curl -sL "https://downloads.apache.org/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz" \
         -o kafka.tgz || \
    curl -sL "https://archive.apache.org/dist/kafka/${KAFKA_VERSION}/kafka_${SCALA_VERSION}-${KAFKA_VERSION}.tgz" \
         -o kafka.tgz

    echo "Extracting..."
    sudo mkdir -p ${KAFKA_DIR}
    sudo tar -xzf kafka.tgz -C ${KAFKA_DIR} --strip-components=1
    rm -f kafka.tgz
else
    echo "[3/6] Kafka already installed at ${KAFKA_DIR}"
fi

# ─── 4. Configure KRaft mode (no Zookeeper) ──────────────────────────────
echo "[4/6] Configuring Kafka KRaft mode..."

sudo mkdir -p ${KAFKA_DATA_DIR}

# Generate a cluster UUID if not already set
CLUSTER_ID_FILE="${KAFKA_DIR}/.cluster-id"
if [ ! -f "${CLUSTER_ID_FILE}" ]; then
    CLUSTER_ID=$(${KAFKA_DIR}/bin/kafka-storage.sh random-uuid)
    echo "${CLUSTER_ID}" | sudo tee "${CLUSTER_ID_FILE}" > /dev/null
else
    CLUSTER_ID=$(cat "${CLUSTER_ID_FILE}")
fi

# Write minimal KRaft server.properties (tuned for t2.micro)
sudo tee ${KAFKA_DIR}/config/kraft/server.properties > /dev/null <<EOF
# ─── KRaft Mode — Single-Node Broker+Controller ───────────────────────
process.roles=broker,controller
node.id=1
controller.quorum.voters=1@localhost:9093
controller.listener.names=CONTROLLER

# ─── Listeners ─────────────────────────────────────────────────────────
listeners=PLAINTEXT://localhost:9092,CONTROLLER://localhost:9093
advertised.listeners=PLAINTEXT://localhost:9092
inter.broker.listener.name=PLAINTEXT
listener.security.protocol.map=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT

# ─── Storage ───────────────────────────────────────────────────────────
log.dirs=${KAFKA_DATA_DIR}

# ─── Resource-Optimized for t2.micro (1 GB RAM) ───────────────────────
# Minimal partitions and replication (single-node)
num.partitions=3
default.replication.factor=1
offsets.topic.replication.factor=1
transaction.state.log.replication.factor=1
transaction.state.log.min.isr=1

# Reduce memory footprint
num.network.threads=2
num.io.threads=2
socket.send.buffer.bytes=65536
socket.receive.buffer.bytes=65536
socket.request.max.bytes=52428800

# Log retention (7 days, small segments)
log.retention.hours=168
log.segment.bytes=52428800
log.retention.check.interval.ms=300000

# Disable auto-topic creation (we create topics via KafkaTopicConfig)
auto.create.topics.enable=true
EOF

# Format storage if not already formatted
if [ ! -f "${KAFKA_DATA_DIR}/meta.properties" ]; then
    echo "Formatting KRaft storage with cluster ID: ${CLUSTER_ID}"
    sudo ${KAFKA_DIR}/bin/kafka-storage.sh format \
         -t "${CLUSTER_ID}" \
         -c ${KAFKA_DIR}/config/kraft/server.properties
fi

sudo chown -R ${KAFKA_USER}:${KAFKA_USER} ${KAFKA_DIR}

# ─── 5. Create systemd service ───────────────────────────────────────────
echo "[5/6] Creating Kafka systemd service..."

sudo tee /etc/systemd/system/kafka.service > /dev/null <<EOF
[Unit]
Description=Apache Kafka (KRaft mode)
Documentation=https://kafka.apache.org
After=network.target
Before=medicalstore-backend.service

[Service]
Type=simple
User=${KAFKA_USER}
Group=${KAFKA_USER}

# ── JVM heap tuned for t2.micro (256 MB max) ──
Environment="KAFKA_HEAP_OPTS=-Xmx256m -Xms128m"
Environment="KAFKA_JVM_PERFORMANCE_OPTS=-XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:+ExplicitGCInvokesConcurrent"

ExecStart=${KAFKA_DIR}/bin/kafka-server-start.sh ${KAFKA_DIR}/config/kraft/server.properties
ExecStop=${KAFKA_DIR}/bin/kafka-server-stop.sh

Restart=on-failure
RestartSec=10
LimitNOFILE=65536
TimeoutStopSec=30

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable kafka

# ─── 6. Configure app to use local Kafka ──────────────────────────────────
echo "[6/6] Configuring MedicalStore to use local Kafka..."

sudo mkdir -p /etc/systemd/system/medicalstore-backend.service.d
sudo tee /etc/systemd/system/medicalstore-backend.service.d/kafka.conf > /dev/null <<EOF
[Service]
Environment="KAFKA_ENABLED=true"
Environment="KAFKA_BOOTSTRAP_SERVERS=localhost:9092"
Environment="JAVA_OPTS=-Xmx384m -Xms256m"
EOF

sudo systemctl daemon-reload

echo ""
echo "═══════════════════════════════════════════════════════════"
echo "  ✅ Kafka installation complete!"
echo ""
echo "  Commands:"
echo "    sudo systemctl start kafka      # Start Kafka"
echo "    sudo systemctl status kafka     # Check status"
echo "    sudo systemctl stop kafka       # Stop Kafka"
echo ""
echo "  Broker:     localhost:9092"
echo "  Mode:       KRaft (no Zookeeper)"
echo "  Heap:       256 MB max"
echo "  Data dir:   ${KAFKA_DATA_DIR}"
echo "  Cost:       \$0 (runs on existing EC2)"
echo "═══════════════════════════════════════════════════════════"
