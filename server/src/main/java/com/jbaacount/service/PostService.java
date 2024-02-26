package com.jbaacount.service;

import com.jbaacount.global.exception.BusinessLogicException;
import com.jbaacount.global.exception.ExceptionMessage;
import com.jbaacount.mapper.PostMapper;
import com.jbaacount.model.Board;
import com.jbaacount.model.Category;
import com.jbaacount.model.Member;
import com.jbaacount.model.Post;
import com.jbaacount.payload.request.PostCreateRequest;
import com.jbaacount.payload.request.PostUpdateRequest;
import com.jbaacount.payload.response.PostMultiResponse;
import com.jbaacount.payload.response.PostResponseForProfile;
import com.jbaacount.payload.response.PostSingleResponse;
import com.jbaacount.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class PostService
{
    private final PostRepository postRepository;
    private final UtilService utilService;
    private final CategoryService categoryService;
    private final BoardService boardService;
    private final VoteService voteService;
    private final FileService fileService;

    @Transactional
    public PostSingleResponse createPost(PostCreateRequest request, List<MultipartFile> files, Long categoryId, Long boardId, Member currentMember)
    {
        Post post = PostMapper.INSTANCE.toPostEntity(request);
        Board board = boardService.getBoardById(boardId);
        utilService.isUserAllowed(board.getIsAdminOnly(), currentMember);

        if(categoryId != null)
        {
            Category category = categoryService.getCategory(categoryId);
            checkBoardHasCategory(board, category);

            utilService.isUserAllowed(category.getIsAdminOnly(), currentMember);
            post.addCategory(category);
        }
        post.addMember(currentMember);
        post.addBoard(board);

        Post savedPost = postRepository.save(post);

        if(files != null && !files.isEmpty())
        {
            fileService.storeFiles(files, savedPost);
        }

        currentMember.getScoreByPost();


        return PostMapper.INSTANCE.toPostSingleResponse(savedPost, false);
    }

    @Transactional
    public PostSingleResponse updatePost(Long postId, PostUpdateRequest request, List<MultipartFile> files, Member currentMember)
    {
        Post post = getPostById(postId);
        //Only the owner of the post has the authority to update
        utilService.isTheSameUser(post.getMember().getId(), currentMember.getId());

        PostMapper.INSTANCE.updatePostFromUpdateRequest(request, post);

        Optional.ofNullable(request.getCategoryId())
                .ifPresent(categoryId ->
                {
                    Category category = categoryService.getCategory(categoryId);
                    utilService.isUserAllowed(category.getIsAdminOnly(), currentMember);
                    Board board = category.getBoard();

                    post.addCategory(category);
                    post.addBoard(board);
                });


        if(files != null && !files.isEmpty())
        {
            fileService.deleteUploadedFile(post);
            fileService.storeFiles(files, post);
        }

        else
        {
            fileService.deleteUploadedFile(post);
            log.info("file removed");
        }

        return PostMapper.INSTANCE.toPostSingleResponse(post, checkIfAlreadyVote(currentMember, post));
    }


    public Post getPostById(Long id)
    {
        return postRepository.findById(id)
                .orElseThrow(() -> new BusinessLogicException(ExceptionMessage.POST_NOT_FOUND));
    }

    public PostSingleResponse getPostSingleResponse(Long id, Member member)
    {
        Post post = getPostById(id);
        boolean voteStatus = checkIfAlreadyVote(member, post);

        return PostMapper.INSTANCE.toPostSingleResponse(post, voteStatus);
    }

    public Page<PostResponseForProfile> getMyPosts(Member member, Pageable pageable)
    {
        log.info("member id = {}", member.toString());
        return postRepository.getPostsByMemberId(member.getId(), pageable);
    }

    @Transactional
    public void deletePostById(Long postId, Member currentMember)
    {
        Post post = getPostById(postId);
        utilService.checkPermission(post.getMember().getId(), currentMember);

        postRepository.deleteById(postId);
    }

    public Page<PostMultiResponse> getPostsByBoardId(Long boardId, String keyword, Pageable pageable)
    {
       return postRepository.getPostsByBoardId(boardId, keyword, pageable);

    }

    public Page<PostMultiResponse> getPostsByCategoryId(Long categoryId, String keyword, Pageable pageable)
    {
        return postRepository.getPostsByCategoryId(categoryId, keyword, pageable);
    }

    private boolean checkIfAlreadyVote(Member member, Post post)
    {
        boolean voteStatus = false;

        if(member != null)
            voteStatus = voteService.existByMemberAndPost(member, post);

        return voteStatus;
    }

    private void checkBoardHasCategory(Board board, Category category)
    {
        if(!board.getCategories().contains(category))
            throw new BusinessLogicException(ExceptionMessage.CATEGORY_NOT_FOUND);
    }
}
