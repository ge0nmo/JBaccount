import React, { useState, Dispatch, SetStateAction } from "react";
import Input from "./../components/Input";
import Button from "./../components/Button";
import { validator, ValidatorStatus } from "../assets/validater";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import { saveAccessToken } from "../assets/tokenActions";

interface Props {
	setIsLogin: Dispatch<React.SetStateAction<boolean>>;
}

const Login = (props: Props) => {
	const api = process.env.REACT_APP_API_URL;
	const navigate = useNavigate();
	const [form, setForm] = useState({
		email: "",
		password: "",
	});
	const [isError, setIsError] = useState({
		email: false,
		password: false,
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
	const handleSubmit = () => {
		setIsError((prevState) => ({
			...prevState,
			email: !validator(validatorStatusEmail),
			password: !validator(validatorStatusPassword),
		}));
		if (isError.email || isError.password) return;
		axios
			.post(`${api}/members/login`, form, { withCredentials: true })
			.then((response) => {
				saveAccessToken(response.headers.authorization);
				props.setIsLogin(true);
				navigate("/");
			})
			.catch((error) => {
				console.error("에러", error);
			});
	};

	return (
		<div className="input_box login_box col_4">
			<h3 className="title_h3">로그인</h3>
			<div className="content">
				<Input
					InputLabel="이메일"
					isLabel={true}
					errorMsg="올바른 이메일을 입력해 주세요."
					inputAttr={{ type: "text", placeholder: "이메일을 입력하세요" }}
					setInputValue={setEmailValue}
					inputValue={form.email}
					isError={isError.email}
				/>
				<Input
					InputLabel="비밀번호"
					isLabel={true}
					errorMsg="올바른 비밀번호를 입력해 주세요."
					inputAttr={{ type: "password", placeholder: "비밀번호를 입력하세요" }}
					setInputValue={setPasswordValue}
					inputValue={form.password}
					isError={isError.password}
				/>
				<Button
					buttonType="primary"
					buttonSize="big"
					buttonLabel="로그인"
					onClick={handleSubmit}
				/>
			</div>
		</div>
	);
};
export default Login;
