package com.adventure.book.integration;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GameControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldStartGameAndGetPersistedState() throws Exception {
        MvcResult startResult = mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": "the-prisoner"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value("the-prisoner"))
                .andExpect(jsonPath("$.health").value(10))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                .andReturn();

        String responseBody = startResult.getResponse().getContentAsString();
        String gameId = JsonPath.read(responseBody, "$.gameId");

        mockMvc.perform(get("/games/{gameId}", gameId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(gameId))
                .andExpect(jsonPath("$.bookId").value("the-prisoner"))
                .andExpect(jsonPath("$.health").value(10))
                .andExpect(jsonPath("$.currentSection.sectionId").value("1"));
    }

    @Test
    void shouldApplyHealthConsequenceAndFinishGame() throws Exception {
        MvcResult startResult = mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": "the-prisoner"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        String gameId = JsonPath.read(startResult.getResponse().getContentAsString(), "$.gameId");

        mockMvc.perform(post("/games/{gameId}/choices", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": "1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSection.sectionId").value("20"))
                .andExpect(jsonPath("$.health").value(10));

        mockMvc.perform(post("/games/{gameId}/choices", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": "0"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSection.sectionId").value("30"))
                .andExpect(jsonPath("$.health").value(4))
                .andExpect(jsonPath("$.appliedConsequence.type").value("LOSE_HEALTH"))
                .andExpect(jsonPath("$.appliedConsequence.value").value(6));

        mockMvc.perform(post("/games/{gameId}/choices", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "optionId": "0"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentSection.sectionId").value("1000"))
                .andExpect(jsonPath("$.currentSection.type").value("END"))
                .andExpect(jsonPath("$.status").value("FINISHED"))
                .andExpect(jsonPath("$.health").value(4));
    }

    @Test
    void shouldReturn400WhenStartingInvalidBookGame() throws Exception {
        mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "bookId": "crystal-caverns"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(
                        "Section '666' in book 'crystal-caverns' is not END and must contain at least one option"
                ));
    }

    @Test
    void shouldMarkGameAsDeadWhenHealthReachesZero() throws Exception {
        MvcResult startResult = mockMvc.perform(post("/games")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "bookId": "the-prisoner"
                            }
                            """))
                .andExpect(status().isCreated())
                .andReturn();

        String gameId = JsonPath.read(startResult.getResponse().getContentAsString(), "$.gameId");

        for (int i = 0; i < 3; i++) {
            mockMvc.perform(post("/games/{gameId}/choices", gameId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "optionId": "0"
                                }
                                """))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/games/{gameId}/choices", gameId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                  "optionId": "0"
                                }
                                """))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(post("/games/{gameId}/choices", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "optionId": "0"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.health").value(1))
                .andExpect(jsonPath("$.currentSection.sectionId").value("500"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        MvcResult deathResult = mockMvc.perform(post("/games/{gameId}/choices", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "optionId": "0"
                            }
                            """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.health").value(0))
                .andExpect(jsonPath("$.status").value("DEAD"))
                .andReturn();

        String body = deathResult.getResponse().getContentAsString();
        Integer health = JsonPath.read(body, "$.health");
        assertThat(health).isZero();

        mockMvc.perform(post("/games/{gameId}/choices", gameId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "optionId": "0"
                            }
                            """))
                .andExpect(status().isBadRequest());
    }
}
