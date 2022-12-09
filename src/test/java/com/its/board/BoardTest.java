package com.its.board;

import com.its.board.dto.BoardDTO;
import com.its.board.dto.CommentDTO;
import com.its.board.entity.BoardEntity;
import com.its.board.entity.CommentEntity;
import com.its.board.repository.BoardRepository;
import com.its.board.repository.CommentRepository;
import com.its.board.service.BoardService;
import static org.assertj.core.api.Assertions.*;

import com.its.board.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@SpringBootTest
public class BoardTest {
    @Autowired
    private BoardService boardService;
    @Autowired
    private BoardRepository boardRepository;
    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentRepository commentRepository;

    private BoardDTO newBoard(int i) {
        BoardDTO boardDTO = new BoardDTO();
        boardDTO.setBoardTitle("title" + i);
        boardDTO.setBoardWriter("writer" + i);
        boardDTO.setBoardPass("pass" + i);
        boardDTO.setBoardContents("contents" + i);
        return boardDTO;
    }

    @Test
    @Transactional
    @Rollback(value = true)
    @DisplayName("글작성 테스트")
    public void boardSaveTest() throws IOException {
        BoardDTO boardDTO = newBoard(1);
        Long savedId = boardService.save(boardDTO);
        BoardDTO findBoard = boardService.findById(savedId);
        assertThat(boardDTO.getBoardWriter()).isEqualTo(findBoard.getBoardWriter());
    }

    @Test
    @Transactional
    @Rollback(value = false)
    @DisplayName("글작성 여러개")
    public void saveList() throws IOException {
        for (int i=1; i<=20; i++) {
            boardService.save(newBoard(i));
        }
//        const temp = (id) => {
//            console.log(id);
//        }
//        IntStream.rangeClosed(21, 40).forEach(i -> {
//            boardService.save(newBoard(i));
//        });
    }

    @Test
    @Transactional
    @DisplayName("연관관계 조회 테스트")
    public void findTest() {
        // 파일이 첨부된 게시글 조회
        BoardEntity boardEntity = boardRepository.findById(23L).get();
        // 첨부파일의 originalFileName 조회
        System.out.println("boardEntity.getBoardFileEntityList() = " + boardEntity.getBoardFileEntityList().get(0).getOriginalFileName());
        // native query
        //
    }

    @Test
    @Transactional
    @Rollback(value = true)
    @DisplayName("댓글작성 테스트")
    public void commentSaveTest() throws IOException {
        // 1. 게시글 작성
        BoardDTO boardDTO = newBoard(100);
        Long savedId = boardService.save(boardDTO);
        // 2. 댓글 작성
        CommentDTO commentDTO = newComment(savedId, 1);
        Long commentSavedId = commentService.save(commentDTO);
        // 3. 저장된 댓글 아이디로 댓글 조회
        CommentEntity commentEntity = commentRepository.findById(commentSavedId).get();
        // 4. 작성자 값이 일치하는지 확인
        assertThat(commentDTO.getCommentWriter()).isEqualTo(commentEntity.getCommentWriter());
    }

    @Test
    @Transactional
    @Rollback(value = true)
    @DisplayName("댓글 목록 테스트")
    public void commentListTest() throws IOException {
        // 1. 게시글 작성
        BoardDTO boardDTO = newBoard(100);
        Long savedId = boardService.save(boardDTO);
        // 2. 해당 게시글에 댓글 3개 작성
        IntStream.rangeClosed(1, 3).forEach(i -> {
            CommentDTO commentDTO = newComment(savedId, i);
            commentService.save(commentDTO);
        });
        // 3. 댓글 목록 조회했을 때 목록 갯수가 3이면 테스트 통과
        List<CommentDTO> commentDTOList = commentService.findAll(savedId);
        assertThat(commentDTOList.size()).isEqualTo(3);
    }

    private CommentDTO newComment(Long boardId, int i) {
        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setCommentWriter("commentWriter" + i);
        commentDTO.setCommentContents("commentContents" + i);
        commentDTO.setBoardId(boardId);
        return commentDTO;
    }


}











