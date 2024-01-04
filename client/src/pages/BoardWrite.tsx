import React, { ChangeEvent } from "react";
import SelectBox from "../components/SelectBox";
import Button from "../components/Button";
import EditorUnit from "../components/EditorUnit";
import boardData from "./../data/boardData.json";
import Input from "../components/Input";
import axios from "axios";
import { useEffect, useState, useRef, ReactElement } from "react";

interface FormType {
	title: string;
	content: string;
	boardId: string;
	[key: string]: string; // 인덱스 시그니처 추가
}

const BoardWrite = () => {
	const api = process.env.REACT_APP_API_URL;
	const [accessToken, setAccessToken] = useState("");
	const [data, setData] = useState<any[]>([]);
	// const [selectItemBoard, setSeletItemBoard] = useState<any[]>([]);
	useEffect(() => {
		const accessTokenInLS = localStorage.getItem("accessToken");
		if (accessTokenInLS) setAccessToken(accessTokenInLS);
		axios
			.get(`${api}/board/all`)
			.then((response) => {
				setData(response.data);
			})
			.catch((error) => {
				console.error("에러", error);
			});
	}, []);
	const [selectValueBoard, setSelectValueBoard] = useState(""); //현재 선택된 게시판
	const [selectItemCategory, setSeletItemCategory] = useState<string[]>([]); //카테고리 목록
	const [selectValueCategory, setSelectValueCategory] = useState(""); //현재 선택된 카테고리
	const [titleValue, setTitleValue] = useState("");
	// const [form, setForm] = useState({
	// 	title: "",
	// 	content: "",
	// 	boardId: -1,
	// });
	const editorRef = useRef<any>(null); //작성된 내용
	//게시판 선택
	const handleSelectboard = (board: string) => {
		setSelectValueBoard(board);
		checkCategory(board);
	};
	//카테고리 유무 확인
	const checkCategory = (board: string) => {
		const matchedBoard = data.filter((el: any) => el.name === board)[0];
		setSeletItemCategory([]);
		if (matchedBoard !== undefined) {
			if (matchedBoard.categories.length !== 0) {
				const categorysArray = matchedBoard.categories.map(
					(el: any) => el.categoryName
				);
				setSeletItemCategory(categorysArray);
			}
		}
	};
	//카테고리 선택
	const handleSelectCategory = (category: string) => {
		setSelectValueCategory(category);
	};
	//제출
	const submitHandler = () => {
		const boardId = data.filter((el) => el.name === selectValueBoard)[0].id; //게시판 id
		const editorData: string = editorRef.current!.getInstance().getHTML(); //작성된 데이터
		const form: FormType = {
			title: titleValue,
			content: editorData,
			boardId: boardId,
		};

		const formData = new FormData();
		formData.append(
			"data",
			new Blob([JSON.stringify(form)], {
				type: "application/json",
			})
		);
		const postAxiosConfig = {
			headers: {
				"Content-Type": "multipart/form-data", // FormData를 사용할 때 Content-Type을 변경
				Authorization: `${localStorage.getItem("accessToken")}`,
			},
		};
		console.log(formData);
		axios
			.post(`${api}/`, formData, postAxiosConfig)
			.then((response) => {
				console.log("글 작성 성공 !!!!", response.data);
			})
			.catch((error) => {
				if (error.response) {
					// 서버 응답이 있을 경우 (에러 상태 코드가 반환된 경우)
					console.error("서버 응답 에러:", error.response.data);
					console.error("응답 상태 코드:", error.response.status);
					console.error("응답 헤더:", error.response.headers);
				} else if (error.request) {
					// 요청이 전혀 되지 않았을 경우
					console.error("요청 에러:", error.request);
				} else {
					// 설정에서 문제가 있어 요청이 전송되지 않은 경우
					console.error("Axios 설정 에러:", error.message);
				}
				console.error("에러 구성:", error.config);
			});
	};
	const handleWriteComplete = () => {
		console.log(boardData);
	};

	return (
		<div className="board_box board_write_box">
			<div className="board_write_head">
				<div className="board_select_wrap">
					<SelectBox
						isLabel={false}
						selectItem={data.map((el: any) => el.name)}
						placeHolder="게시판 선택"
						setHandleStatus={handleSelectboard}
					/>
					{selectItemCategory.length !== 0 ? (
						<SelectBox
							isLabel={false}
							selectItem={selectItemCategory}
							placeHolder="카테고리 선택"
							setHandleStatus={handleSelectCategory}
						/>
					) : null}
				</div>
				<div className="board_input_title">
					<Input
						InputLabel="제목"
						isLabel={false}
						errorMsg="제목을 입력해 주세요."
						inputAttr={{
							type: "text",
							placeholder: "게시글 제목을 입력하세요",
						}}
						setInputValue={setTitleValue}
						inputValue={titleValue}
					/>
				</div>
			</div>
			<EditorUnit ref={editorRef} />
			<div className="board_editor_button_wrap">
				<Button
					buttonType="primary"
					buttonSize="big"
					buttonLabel="작성 완료"
					onClick={submitHandler}
				/>
				<Button buttonType="no_em" buttonSize="big" buttonLabel="취소" />
			</div>
		</div>
	);
};
export default BoardWrite;
