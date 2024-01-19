package com.jbaacount.mapper;

import com.jbaacount.model.Board;
import com.jbaacount.model.Member;
import com.jbaacount.model.Post;
import com.jbaacount.payload.request.PostCreateRequest;
import com.jbaacount.payload.response.PostMultiResponse;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-01-19T15:42:59+0900",
    comments = "version: 1.5.3.Final, compiler: IncrementalProcessingEnvironment from gradle-language-java-8.1.1.jar, environment: Java 17.0.7 (Azul Systems, Inc.)"
)
@Component
public class PostMapperImpl implements PostMapper {

    @Override
    public Post toPostEntity(PostCreateRequest request) {
        if ( request == null ) {
            return null;
        }

        Post.PostBuilder post = Post.builder();

        post.title( request.getTitle() );
        post.content( request.getContent() );

        return post.build();
    }

    @Override
    public PostMultiResponse toPostMultiResponse(Post post, String timeInfo) {
        if ( post == null && timeInfo == null ) {
            return null;
        }

        PostMultiResponse postMultiResponse = new PostMultiResponse();

        if ( post != null ) {
            postMultiResponse.setMemberId( postMemberId( post ) );
            postMultiResponse.setMemberName( postMemberNickname( post ) );
            postMultiResponse.setBoardId( postBoardId( post ) );
            postMultiResponse.setBoardName( postBoardName( post ) );
            postMultiResponse.setId( post.getId() );
            postMultiResponse.setTitle( post.getTitle() );
            postMultiResponse.setContent( post.getContent() );
            postMultiResponse.setCreatedAt( post.getCreatedAt() );
        }
        postMultiResponse.setTimeInfo( timeInfo );
        postMultiResponse.setCommentsCount( post.getComments().size() );

        return postMultiResponse;
    }

    private Long postMemberId(Post post) {
        if ( post == null ) {
            return null;
        }
        Member member = post.getMember();
        if ( member == null ) {
            return null;
        }
        Long id = member.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String postMemberNickname(Post post) {
        if ( post == null ) {
            return null;
        }
        Member member = post.getMember();
        if ( member == null ) {
            return null;
        }
        String nickname = member.getNickname();
        if ( nickname == null ) {
            return null;
        }
        return nickname;
    }

    private Long postBoardId(Post post) {
        if ( post == null ) {
            return null;
        }
        Board board = post.getBoard();
        if ( board == null ) {
            return null;
        }
        Long id = board.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }

    private String postBoardName(Post post) {
        if ( post == null ) {
            return null;
        }
        Board board = post.getBoard();
        if ( board == null ) {
            return null;
        }
        String name = board.getName();
        if ( name == null ) {
            return null;
        }
        return name;
    }
}