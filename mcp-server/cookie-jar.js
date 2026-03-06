/**
 * Lightweight cookie jar for managing HTTP session cookies.
 * Used to maintain authentication state across MCP tool calls.
 */
export class CookieJar {
    constructor() {
        /** @type {Map<string, string>} */
        this.cookies = new Map();
    }

    /**
     * Parse and store a Set-Cookie header value.
     * @param {string} setCookieHeader
     */
    addCookie(setCookieHeader) {
        const parts = setCookieHeader.split(";")[0].trim();
        const eqIdx = parts.indexOf("=");
        if (eqIdx > 0) {
            const name = parts.substring(0, eqIdx).trim();
            const value = parts.substring(eqIdx + 1).trim();
            this.cookies.set(name, value);
        }
    }

    /**
     * Build the Cookie header value from all stored cookies.
     * @returns {string}
     */
    getCookieHeader() {
        const pairs = [];
        for (const [name, value] of this.cookies) {
            pairs.push(`${name}=${value}`);
        }
        return pairs.join("; ");
    }

    /**
     * Check if a specific cookie exists.
     * @param {string} name
     * @returns {boolean}
     */
    hasCookie(name) {
        return this.cookies.has(name);
    }

    /**
     * Get the total number of cookies stored.
     * @returns {number}
     */
    count() {
        return this.cookies.size;
    }

    /** Clear all stored cookies. */
    clear() {
        this.cookies.clear();
    }
}
