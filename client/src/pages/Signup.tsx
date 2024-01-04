import { useState } from "react";
import Input from "./../components/Input";
import Button from "./../components/Button";
import { validator, ValidatorStatus } from "../assets/validater";
import axios from "axios";

const Signup = () => {
	const api = process.env.REACT_APP_API_URL;
	const [form, setForm] = useState({
		email: "",
		password: "",
		nickname: "",
	});
	const [isError, setIsError] = useState({
		email: false,
		password: false,
		nickname: false,
	});
	const setEmailValue = (value: string) => {
		setForm((prevState) => ({
			...prevState,
			email: value,
		}));
	};
	const setPasswordValue = (value: string) => {
		setForm((prevState) => ({
			...prevState,
			password: value,
		}));
	};
	const setNicknameValue = (value: string) => {
		setForm((prevState) => ({
			...prevState,
			nickname: value,
		}));
	};
	//유효성 검사
	const validatorStatusEmail: ValidatorStatus = {
		value: form.email,
		isRequired: true,
		valueType: "email",
	};
	const validatorStatusPassword: ValidatorStatus = {
		value: form.password,
		isRequired: true,
		valueType: "password",
	};
	const validatorStatusNickname: ValidatorStatus = {
		value: form.nickname,
		isRequired: true,
		valueType: "nickname",
	};
	const handleSubmit = () => {
		setIsError((prevState) => ({
			...prevState,
			email: !validator(validatorStatusEmail),
			password: !validator(validatorStatusPassword),
			nickname: !validator(validatorStatusNickname),
		}));
		if (isError.email || isError.password || isError.nickname) {
			return;
		}
		console.log(form);

		axios
			.post(`${api}/members/sign-up`, form, { withCredentials: true })
			.then((response) => {
				console.log("회원가입 성공 !!!!", response.data);
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
	return (
		<div className="input_box sign_box col_4">
			<h3 className="title_h3">회원가입</h3>
			<div className="validate_email_wrap">
				<Input
					InputLabel="이메일"
					isLabel={true}
					errorMsg="올바른 이메일을 입력해 주세요."
					inputAttr={{ type: "text", placeholder: "이메일을 입력하세요" }}
					setInputValue={setEmailValue}
					inputValue={form.email}
					isError={isError.email}
				/>
				{/* <Button
					buttonType="primary"
					buttonSize="big"
					buttonLabel="이메일 인증"
				/>
				<p className="validate_email_msg">이메일 인증이 완료되었습니다.</p>
				<p className="validate_email_msg error">이미 가입한 이메일입니다.</p> */}
			</div>
			<div className="content">
				<Input
					InputLabel="비밀번호 입력"
					isLabel={true}
					errorMsg="비밀번호는 8~20자의 영문, 숫자가 포함되어야 합니다. "
					inputAttr={{ type: "password", placeholder: "비밀번호를 입력하세요" }}
					setInputValue={setPasswordValue}
					inputValue={form.password}
					isError={isError.password}
				/>
				<Input
					InputLabel="비밀번호 확인"
					isLabel={true}
					errorMsg="비밀번호가 일치하지 않습니다."
					inputAttr={{
						type: "password",
						placeholder: "비밀번호를 한 번 더 입력하세요",
					}}
				/>
				<Input
					InputLabel="닉네임"
					isLabel={true}
					errorMsg="공백 포함 2~10자 사이의 영문, 한글, 숫자만 가능합니다."
					inputAttr={{
						type: "text",
						placeholder: "닉네임을 입력하세요",
					}}
					setInputValue={setNicknameValue}
					inputValue={form.nickname}
					isError={isError.nickname}
				>
					<Button
						buttonType="another"
						buttonSize="big"
						buttonLabel="중복 확인"
					/>
				</Input>
				<Button
					buttonType="primary"
					buttonSize="big"
					buttonLabel="회원가입"
					onClick={handleSubmit}
				/>
			</div>
		</div>
	);
};
export default Signup;
