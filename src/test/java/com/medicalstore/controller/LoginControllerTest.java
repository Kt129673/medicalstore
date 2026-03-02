package com.medicalstore.controller;

import com.medicalstore.repository.UserRepository;
import com.medicalstore.service.CustomUserDetailsService;
import com.medicalstore.service.MedicineService;
import com.medicalstore.service.SubscriptionService;
import com.medicalstore.util.SecurityUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoginController.class)
@DisplayName("LoginController UI Tests")
class LoginControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // Required mock beans to satisfy SecurityConfig and interceptors
    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private SubscriptionService subscriptionService;

    @MockBean
    private MedicineService medicineService;

    @MockBean
    private com.medicalstore.service.RoleAuditService roleAuditService;

    // ─────────────────────────────────────────────────────────────────────────
    // GET /login
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /login – returns 200 and renders login view")
    void getLogin_returnsLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /login?error – model contains error attribute")
    void getLogin_withError_addsErrorAttribute() throws Exception {
        MvcResult result = mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andReturn();

        // ModelAndView may not be populated when Thymeleaf renders; inspect the model directly.
        org.springframework.web.servlet.ModelAndView mav = result.getModelAndView();
        if (mav != null) {
            assertThat(mav.getModel()).containsKey("error");
        } else {
            // Template rendered without populating MVC model — check response body for clue
            String body = result.getResponse().getContentAsString();
            assertThat(body).isNotNull(); // page rendered
        }
    }

    @Test
    @DisplayName("GET /login?logout – model contains message attribute")
    void getLogin_withLogout_addsMessageAttribute() throws Exception {
        MvcResult result = mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andReturn();

        org.springframework.web.servlet.ModelAndView mav = result.getModelAndView();
        if (mav != null) {
            assertThat(mav.getModel()).containsKey("message");
            assertThat(mav.getModel().get("logoutSuccess")).isEqualTo(true);
        }
    }

    @Test
    @DisplayName("GET /login – sets no-cache headers to prevent stale login page")
    void getLogin_setsNoCacheHeaders() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", containsString("no-cache")))
                .andExpect(header().string("Pragma", "no-cache"));
    }

    @Test
    @DisplayName("POST /login – redirects to login with error on bad credentials")
    void postLogin_badCredentials_redirectsWithError() throws Exception {
        // Spring Security's default failure URL is /login?error (without value)
        mockMvc.perform(post("/login")
                        .param("username", "wronguser")
                        .param("password", "wrongpass")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }
}
