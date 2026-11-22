package edu.fiu.cen4021.elevate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class WebFlowTest {
    @Autowired MockMvc mvc;

    @Test void tc01_validEmployeeLogin() throws Exception {
        mvc.perform(post("/login").with(csrf())
                .param("username", "frontdesk").param("password", "Elevate123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test void tc02_invalidEmployeeLogin() throws Exception {
        mvc.perform(post("/login").with(csrf())
                .param("username", "frontdesk").param("password", "wrong"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test void tc03_emptyLoginIsDeniedByServer() throws Exception {
        mvc.perform(post("/login").with(csrf())
                .param("username", "").param("password", ""))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
        // The exact empty-fields message is also displayed client-side in login.js.
    }

    @Test void tc04_searchMemberById() throws Exception {
        mvc.perform(get("/api/members").with(user("frontdesk").roles("FRONT_DESK"))
                .param("query", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Yuji Itadori"));
    }

    @Test void tc05_searchMemberByPartialName() throws Exception {
        mvc.perform(get("/api/members").with(user("frontdesk").roles("FRONT_DESK"))
                .param("query", "gOjO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fullName").value("Satoru Gojo"));
    }

    @Test void tc06_searchUnknownMember() throws Exception {
        mvc.perform(get("/api/members").with(user("frontdesk").roles("FRONT_DESK"))
                .param("query", "not-a-member"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test void tc07_emptySearchReturnsValidationMessage() throws Exception {
        mvc.perform(get("/api/members").with(user("frontdesk").roles("FRONT_DESK"))
                .param("query", "   "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Please enter an ID or name before searching."));
    }

    @Test void protectedMemberSearchRedirectsUnauthenticatedUsers() throws Exception {
        mvc.perform(get("/members"))
                .andExpect(status().is3xxRedirection());
    }

    @Test void memberSelectionHandoffUsesSameId() throws Exception {
        mvc.perform(get("/members/1001/selected")
                .with(user("frontdesk").roles("FRONT_DESK")))
                .andExpect(status().isOk())
                .andExpect(view().name("selected-member"))
                .andExpect(model().attributeExists("member"));
    }

    @Test void memberIdLookupReturnsMatchingMember() throws Exception {
        mvc.perform(get("/api/members/1007")
                .with(user("frontdesk").roles("FRONT_DESK")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Natsu Dragneel"))
                .andExpect(jsonPath("$.membershipStatus").value("Active"));
    }
}
