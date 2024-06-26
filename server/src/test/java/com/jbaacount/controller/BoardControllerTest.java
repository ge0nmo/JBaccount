package com.jbaacount.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jbaacount.dummy.DummyObject;
import com.jbaacount.model.Board;
import com.jbaacount.model.Member;
import com.jbaacount.model.type.BoardType;
import com.jbaacount.repository.BoardRepository;
import com.jbaacount.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Sql("classpath:db/teardown.sql")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class BoardControllerTest extends DummyObject
{
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private BoardRepository boardRepository;


    @BeforeEach
    void setUp()
    {
        Member member = newMockMember(1L, "abc@naver.com", "mockUser", "ADMIN");
        memberRepository.save(member);

        Board board1 = boardRepository.save(newMockBoard(1L, "board1", 1));

        boardRepository.save(newMockBoard(2L, "board2", 2));

        Board category1 = newMockBoard(3L, "category1", 1);
        Board category2 = newMockBoard(4L, "category2", 2);

        category1.addParent(board1);
        category2.addParent(board1);

        boardRepository.save(category1);
        boardRepository.save(category2);
    }

    @Test
    void getMenu() throws Exception
    {
        // given

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/board/menu"));

        String responseBody = objectMapper.writeValueAsString(resultActions.andReturn().getResponse().getContentAsString());

        System.out.println("response body = " + responseBody);
        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1L))
                .andExpect(jsonPath("$.data[0].name").value("board1"))
                .andExpect(jsonPath("$.data[0].type").value(BoardType.BOARD.getCode()))
                .andExpect(jsonPath("$.data[0].category[0].id").value(3L))
                .andExpect(jsonPath("$.data[0].category[0].name").value("category1"))
                .andExpect(jsonPath("$.data[0].category[0].type").value(BoardType.CATEGORY.getCode()))
                .andExpect(jsonPath("$.data[0].category[1].id").value(4L))
                .andExpect(jsonPath("$.data[0].category[1].name").value("category2"))
                .andExpect(jsonPath("$.data[0].category[1].type").value(BoardType.CATEGORY.getCode()))


                .andExpect(jsonPath("$.data[1].id").value(2L))
                .andExpect(jsonPath("$.data[1].name").value("board2"))
                .andExpect(jsonPath("$.data[1].type").value(BoardType.BOARD.getCode()));

    }

    @Test
    void getBoardById() throws Exception
    {
        // given

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/board/single-info/" + 1));

        String responseBody = objectMapper.writeValueAsString(resultActions.andReturn().getResponse().getContentAsString());

        System.out.println("response body = " + responseBody);
        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.name").value("board1"));
    }

    @Test
    void getCategoryList() throws Exception
    {
        // given

        // when
        ResultActions resultActions = mvc
                .perform(get("/api/v1/board/category/" + 1));

        String responseBody = objectMapper.writeValueAsString(resultActions.andReturn().getResponse().getContentAsString());

        System.out.println("response body = " + responseBody);
        // then
        resultActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(3L))
                .andExpect(jsonPath("$.data[0].name").value("category1"))
                .andExpect(jsonPath("$.data[0].orderIndex").value(1))

                .andExpect(jsonPath("$.data[1].id").value(4L))
                .andExpect(jsonPath("$.data[1].name").value("category2"))
                .andExpect(jsonPath("$.data[1].orderIndex").value(2));
    }
}