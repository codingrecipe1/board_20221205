package com.its.board;

import com.its.board.dto.BoardDTO;
import com.its.board.dto.CommentDTO;
import com.its.board.dto.MemberDTO;
import com.its.board.entity.BoardEntity;
import com.its.board.entity.CommentEntity;
import com.its.board.entity.MemberEntity;
import com.its.board.repository.BoardRepository;
import com.its.board.repository.CommentRepository;
import com.its.board.repository.MemberRepository;
import com.its.board.service.BoardService;
import static org.assertj.core.api.Assertions.*;

import com.its.board.service.CommentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    @Autowired
    private MemberRepository memberRepository;

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

    @Test
    @Transactional
    @Rollback(value = true)
    @DisplayName("페이징 객체 확인")
    public void pagingParams() {
        int page = 0;
        Page<BoardEntity> boardEntities = boardRepository.findAll(PageRequest.of(page, 3, Sort.by(Sort.Direction.DESC, "id")));
        // Page 객체가 제공해주는 메서드 확인
        System.out.println("boardEntities.getContent() = " + boardEntities.getContent()); // 요청페이지에 들어있는 데이터
        System.out.println("boardEntities.getTotalElements() = " + boardEntities.getTotalElements()); // 전체 글갯수
        System.out.println("boardEntities.getNumber() = " + boardEntities.getNumber()); // 요청페이지(jpa 기준)
        System.out.println("boardEntities.getTotalPages() = " + boardEntities.getTotalPages()); // 전체 페이지 갯수
        System.out.println("boardEntities.getSize() = " + boardEntities.getSize()); // 한페이지에 보여지는 글갯수
        System.out.println("boardEntities.hasPrevious() = " + boardEntities.hasPrevious()); // 이전페이지 존재 여부
        System.out.println("boardEntities.isFirst() = " + boardEntities.isFirst()); // 첫페이지인지 여부
        System.out.println("boardEntities.isLast() = " + boardEntities.isLast()); // 마지막페이지인지 여부

        // Page<BoardEntity> -> Page<BoardDTO>
        Page<BoardDTO> boardList = boardEntities.map(
                // boardEntities에 담긴 boardEntity 객체를 board에 담아서
                // boardDTO 객체로 하나씩 옮겨 담는 과정
                board -> new BoardDTO(board.getId(),
                                        board.getBoardWriter(),
                                        board.getBoardTitle(),
                                        board.getCreatedTime(),
                                        board.getBoardHits()
                                     )
        );

        System.out.println("boardList.getContent() = " + boardList.getContent()); // 요청페이지에 들어있는 데이터
        System.out.println("boardList.getTotalElements() = " + boardList.getTotalElements()); // 전체 글갯수
        System.out.println("boardList.getNumber() = " + boardList.getNumber()); // 요청페이지(jpa 기준)
        System.out.println("boardList.getTotalPages() = " + boardList.getTotalPages()); // 전체 페이지 갯수
        System.out.println("boardList.getSize() = " + boardList.getSize()); // 한페이지에 보여지는 글갯수
        System.out.println("boardList.hasPrevious() = " + boardList.hasPrevious()); // 이전페이지 존재 여부
        System.out.println("boardList.isFirst() = " + boardList.isFirst()); // 첫페이지인지 여부
        System.out.println("boardList.isLast() = " + boardList.isLast()); // 마지막페이지인지 여부

    }

    @Test
    @Transactional
    @DisplayName("검색 테스트")
    public void searchTest() {
        String q = "a";
        // 제목이나 작성자에 a 가 포함된 검색
        List<BoardEntity> boardEntityList =
                boardRepository.findByBoardTitleContainingOrBoardWriterContainingOrderByIdDesc(q, q);
        for (BoardEntity boardEntity: boardEntityList) {
            BoardDTO boardDTO = BoardDTO.toDTO(boardEntity);
            System.out.println("boardDTO = " + boardDTO);
        }
    }

    @Test
    @Transactional
    @Rollback(value = false)
    @DisplayName("set null 테스트")
    public void setNullTest() throws IOException {
        /*
            1. 회원가입
            2. 위에서 가입한 회원이 게시글 작성
            3. 회원삭제
            4. board_table 확인
         */

        // 1.
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setMemberEmail("email30");
        memberDTO.setMemberPassword("password1");
        memberDTO.setMemberName("name1");
        memberDTO.setMemberAge(11);
        memberDTO.setMemberPhone("010-1111-1111");

        MemberEntity memberEntity = MemberEntity.toSaveEntity(memberDTO);
        Long memberId = memberRepository.save(memberEntity).getId();

        // 2.
        BoardDTO newBoard = new BoardDTO();
        newBoard.setBoardWriter(memberDTO.getMemberEmail());
        newBoard.setBoardTitle("회원 삭제용 제목");
        newBoard.setBoardPass("회원 삭제용 비번");
        newBoard.setBoardContents("회원 삭제용 내용");
        Long savedId = boardService.save(newBoard);

        // 3.
//        memberRepository.deleteById(memberId);
//        memberDelete(memberId);
    }

    @Test
    @Transactional
    @Rollback(value = false)
    public void memberDelete() {
        memberRepository.deleteById(26L);
    }


}











