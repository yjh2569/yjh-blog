package com.yjh.blog.web;

import com.yjh.blog.domain.user.Role;
import com.yjh.blog.domain.user.User;
import com.yjh.blog.domain.user.UserRepository;
import com.yjh.blog.web.dto.comments.CommentsSaveRequestDto;
import com.yjh.blog.web.dto.comments.CommentsUpdateRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yjh.blog.domain.comments.Comments;
import com.yjh.blog.domain.comments.CommentsRepository;
import com.yjh.blog.domain.posts.Posts;
import com.yjh.blog.domain.posts.PostsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CommentsApiControllerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private CommentsRepository commentsRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @BeforeEach
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    public void tearDown() throws Exception {
        commentsRepository.deleteAll();
        postsRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles="USER")
    public void Comments_????????????() throws Exception {
        //given
        String title = "title";
        String content = "content";
        Posts post = Posts.builder()
                .title(title)
                .content(content)
                .classification("html")
                .build();
        postsRepository.save(post);

        String name = "name";
        String email = "email";
        String picture = "picture";
        Role role = Role.USER;
        User user = User.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .role(role)
                .build();
        userRepository.save(user);

        String commentsContent = "????????? ?????? ??????";

        CommentsSaveRequestDto requestDto = CommentsSaveRequestDto.builder()
                .content(commentsContent)
                .post_id(post.getId())
                .user_id(user.getId())
                .created_by(user.getName())
                .build();

        String url = "http://localhost:"+port+"/api/v1/comments";

        //when
        // ????????? MockMvc??? ?????? API??? ???????????????. ?????? ????????? ???????????? ???????????? ?????? ObjectMapper??? ?????? ????????? JSON?????? ????????????.
        mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andDo(print()).andExpect(status().isOk());

        //then
        List<Comments> all = commentsRepository.getCommentsOfPost(post.getId());
        assertThat(all.get(0).getContent()).isEqualTo(commentsContent);
    }

    @Test
    @WithMockUser(roles="USER")
    public void Comments_????????????() throws Exception {
        //given
        String title = "title";
        String content = "content";
        Posts post = Posts.builder()
                .title(title)
                .content(content)
                .classification("html")
                .build();
        postsRepository.save(post);

        String name = "name";
        String email = "email";
        String picture = "picture";
        Role role = Role.USER;
        User user = User.builder()
                .name(name)
                .email(email)
                .picture(picture)
                .role(role)
                .build();
        userRepository.save(user);

        String commentsContent = "????????? ?????? ??????";
        Comments savedComment = commentsRepository.save(Comments.builder()
                .content(commentsContent)
                .post(post)
                .user(user)
                .created_by(user.getName())
                .build());

        Long updateId = savedComment.getId();
        String expectedContent = "????????? ?????? ??????2";

        CommentsUpdateRequestDto requestDto = CommentsUpdateRequestDto.builder()
                .content(expectedContent)
                .created_by(user.getName())
                .post_id(post.getId())
                .user_id(user.getId())
                .build();

        String url = "http://localhost:"+port+"/api/v1/comments/"+updateId;

        HttpEntity<CommentsUpdateRequestDto> requestEntity = new HttpEntity<>(requestDto);

        //when
        // ????????? MockMvc??? ?????? API??? ???????????????. ?????? ????????? ???????????? ???????????? ?????? ObjectMapper??? ?????? ????????? JSON?????? ????????????.
        mvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        //then
        List<Comments> all = commentsRepository.getCommentsOfPost(post.getId());
        assertThat(all.get(0).getContent()).isEqualTo(expectedContent);
    }
}
