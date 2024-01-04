import { useState } from "react";
import BoardItem from "../components/Boarditem";
import boardListData from "../data/boardListData.json";

const BoardList = () => {
	//의사코드
	/*
	0. 필요한 변수
	- 전체 페이지 수 totalPages
	- 한번에 보여줄 최대 페이지네이션 버튼 수 maxPage: 기본값 5
	- 현재 페이지 nowPage: 기본값 1
	- 한 페이지에 보여줄 최대 아이템 수 maxItem: 기본값 8
	- 현재 페이지네이션 인덱스 배열 pagination
	1. 한 페이지의 아이템 렌더링
	2. 페이지네이션 버튼 수 렌더링
	3. 페이지네이션 클릭이벤트
	4. 이전 다음 버튼 클릭이벤트
	*/
	const totalPages = boardListData.pageInfo.totalPages;
	const maxPageValue = 5;
	const maxPage = totalPages >= maxPageValue ? maxPageValue : totalPages;
	const [nowPage, setNowPage] = useState(1);
	const [pagenation, setPagenation] = useState(generatePageRange(1, maxPage));

	//페이지네이션 범위 설정 함수
	function generatePageRange(start: number, end: number) {
		return Array.from({ length: end + 1 - start }, (_, i) => start + i);
	}

	const handlaClickPagination = (page: number) => {
		if (nowPage !== page) setNowPage(page);
	};

	const handleClickPrev = () => {
		/* 
		- 현재 페이지가 첫번째 페이지네이션(현재 페이지%maxPage===1)이거나 1인지 판별해야 한다.
		1. 현재 페이지가 1이라면: 가장 처음 페이지므로 아무것도 바꾸지 않는다.
		2. 현재 페이지%maxPage===1 이라면: 페이지네이션 범위(시작, 끝)를 ([현재 페이지-maxPage], [현재 페이지-1])로 바꾸어야 한다.
		3. 그 외를 포함해 [현재 페이지-1]로 설정한다.
		*/
		if (nowPage === 1) return;
		if (nowPage % maxPage === 1)
			setPagenation(generatePageRange(nowPage - maxPage, nowPage - 1));
		setNowPage(nowPage - 1);
	};
	const handleClickNext = () => {
		/*
		- 현재 페이지가 마지막 페이지네이션(현재 페이지%maxPage===0)이거나 마지막 페이지인지 판별해야 한다.
		1. 현재 페이지가 마지막 페이지(totalPages)라면: 가장 마지막 페이지므로 아무것도 바꾸지 않는다.
		2. 현재 페이지%maxPage===0 이라면: 페이지네이션 범위(시작, 끝)를 ([현재 페이지+1], [현재 페이지+maxPage])로 바꾸어야 한다.
			2.1. 다음 페이지네이션이 maxPage보다 작은 경우가 있으므로 이를 처리한다.
			- [현재 페이지+maxPage]>=totalPages 라면: 범위를 ([현재 페이지+1], totalPages)로 설정한다.
		3. 그 외를 포함해 [현재 페이지+1]로 설정한다.
		*/
		if (nowPage === totalPages) return;
		if (nowPage % maxPage === 0) {
			if (nowPage + maxPage >= totalPages)
				setPagenation(generatePageRange(nowPage + 1, totalPages));
			else setPagenation(generatePageRange(nowPage + 1, nowPage + maxPage));
		}
		setNowPage(nowPage + 1);
	};

	return (
		<div>
			<h3>게시판 이름</h3>
			<ul className="board_box board_list_box">
				{boardListData.data.map((el, idx) => {
					return (
						<li key={idx}>
							<BoardItem data={el} />
						</li>
					);
				})}
			</ul>
			<div className="pagination">
				<button className="arrow left" onClick={handleClickPrev}>
					이전
				</button>
				<ul className="page_num">
					{pagenation.map((el, idx) => {
						return (
							<li
								className={
									el === nowPage ? "page_button active" : "page_button"
								}
								key={idx}
								onClick={() => handlaClickPagination(el)}
							>
								{el}
							</li>
						);
					})}
				</ul>
				<button className="arrow right" onClick={handleClickNext}>
					다음
				</button>
			</div>
		</div>
	);
};
export default BoardList;
