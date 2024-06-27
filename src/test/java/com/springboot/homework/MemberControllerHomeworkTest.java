package com.springboot.homework;

import com.springboot.member.dto.MemberDto;
import com.google.gson.Gson;
import com.springboot.member.dto.MemberPatchDto;
import com.springboot.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

import static com.springboot.member.entity.Member.MemberStatus.MEMBER_ACTIVE;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class MemberControllerHomeworkTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Gson gson;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void postMemberTest() throws Exception {
        // given
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com",
                "홍길동",
                "010-1234-5678");
        String content = gson.toJson(post);

        // when
        ResultActions actions =
                mockMvc.perform(
                        post("/v11/members")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(content)
                );

        // then
        actions
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", is(startsWith("/v11/members/"))));
    }

    @Test
    void patchMemberTest() throws Exception {
        // TODO MemberController의 patchMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        // given
        // memberController의 patchMember()를 테스트하기 위해서,
        // MockMvc를 이용해 stub 데이터를 생성 후 진행
        MemberDto.Post postMemberDto = new MemberDto.Post("hgd@gmail.com",
                "홍길동",
                "010-1234-5678");
        String postContent = gson.toJson(postMemberDto);

        // when
        ResultActions postActions =
                mockMvc.perform(
                post("/v11/members")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(postContent)
        );
        String location = postActions.andReturn().getResponse().getHeader("Location");

        MemberDto.Patch patchMemberDto = MemberDto.Patch.builder()
                .phone("010-9876-5421")
                .build();

        URI patchUri = UriComponentsBuilder.newInstance().path(location).build().toUri();

        ResultActions patchActions = mockMvc.perform(
                        patch(patchUri)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(patchMemberDto))
        );

        // then
        patchActions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.phone").value(patchMemberDto.getPhone()));
    }

    @Test
    void getMemberTest() throws Exception {
        // given: MemberController의 getMember()를 테스트하기 위해서 postMember()를 이용해 테스트 데이터를 생성 후, DB에 저장
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com","홍길동","010-1111-1111");
        String postContent = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );

        long memberId;
        String location = postActions.andReturn().getResponse().getHeader("Location"); // "/v11/members/1"
        memberId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when / then
        mockMvc.perform(
                        get("/v11/members/" + memberId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(post.getEmail()))
                .andExpect(jsonPath("$.data.name").value(post.getName()))
                .andExpect(jsonPath("$.data.phone").value(post.getPhone()));
    }

    @Test
    void getMembersTest() throws Exception {
        // TODO MemberController의 getMembers() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        // given
        MemberDto.Post post1 = new MemberDto.Post("hgd@gmail.com","홍길동1","010-1111-1111");
        MemberDto.Post post2 = new MemberDto.Post("sdf@gmail.com","홍길동2","010-2222-2222");

        String postContent1 = gson.toJson(post1);
        String postContent2 = gson.toJson(post2);

                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent1)
                );

                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent2)
                );

        // when / then
        MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("page", "1");
        multiValueMap.add("size", "10");

        mockMvc.perform(
                        get("/v11/members/")
                                .params(multiValueMap)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].email").value(post2.getEmail()))
                .andExpect(jsonPath("$.data[0].name").value(post2.getName()))
                .andExpect(jsonPath("$.data[0].phone").value(post2.getPhone()))
                .andExpect(jsonPath("$.data[1].email").value(post1.getEmail()))
                .andExpect(jsonPath("$.data[1].name").value(post1.getName()))
                .andExpect(jsonPath("$.data[1].phone").value(post1.getPhone()));
    }

    @Test
    void deleteMemberTest() throws Exception {
        // TODO MemberController의 deleteMember() 핸들러 메서드를 테스트하는 테스트 케이스를 여기에 작성하세요.
        // given
        MemberDto.Post post = new MemberDto.Post("hgd@gmail.com","홍길동1","010-1111-1111");
        String postContent = gson.toJson(post);

        ResultActions postActions =
                mockMvc.perform(
                        post("/v11/members")
                                .contentType(MediaType.APPLICATION_JSON)
                                .accept(MediaType.APPLICATION_JSON)
                                .content(postContent)
                );

        long memberId;
        String location = postActions.andReturn().getResponse().getHeader("Location"); // "/v11/members/1"
        memberId = Long.parseLong(location.substring(location.lastIndexOf("/") + 1));

        // when / then
        mockMvc.perform(
                        delete("/v11/members/" + memberId)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNoContent());
    }
}
