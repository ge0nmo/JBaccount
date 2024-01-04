import React, { ReactNode } from "react";
import { useState, useRef, useEffect } from "react";
import "../App.css";

export interface SelectProps {
	id?: string;
	selectLabel?: string;
	isLabel: boolean; //label의 유무를 나타냅니다.
	selectItem?: string[];
	placeHolder: string;
	setHandleStatus?: (status: string) => void;
	children?: ReactNode;
}

const SelectBox = ({
	id,
	selectLabel,
	isLabel,
	selectItem,
	placeHolder,
	setHandleStatus,
	children,
}: SelectProps) => {
	const [nowValue, setNowValue] = useState(placeHolder);
	const [isOpen, setIsOpen] = useState(false);
	const customSelectRef = useRef<HTMLDivElement | null>(null);
	//셀렉트 박스 외부 클릭시 닫힘
	const handleClickAnother = (event: MouseEvent) => {
		if (
			customSelectRef.current &&
			!customSelectRef.current.contains(event.target as Node)
		) {
			setIsOpen(false);
		}
	};
	//메모리 누수 방지 처리: 컴포넌트가 마운트 될 때 이벤트를 등록하고, 이외에는 제거
	useEffect(() => {
		document.addEventListener("click", handleClickAnother);
		return () => {
			document.removeEventListener("click", handleClickAnother);
		};
	});
	const handleClickSelectBody = () => {
		setIsOpen(!isOpen);
	};
	const handleClickListItem = (event: React.MouseEvent<HTMLLIElement>) => {
		const value = (event.target as HTMLLIElement).innerText;
		setNowValue(value);
		if (setHandleStatus !== undefined) setHandleStatus(value);
		setIsOpen(false);
	};

	return (
		<div className="select_wrap" id={id}>
			{/* 기본 select */}
			{/* <div className="origin_select">
				{isLabel ? <label htmlFor={id}>{selectLabel}</label> : null}
				<select>
					<option value="">{placeHolder}</option>
					{selectItem.map((value, idx) => (
						<option value={value} key={idx}>
							{value}
						</option>
					))}
				</select>
			</div> */}
			<div
				className={`custom_select ${isOpen ? "active" : null}`}
				ref={customSelectRef}
			>
				{isLabel ? <p className="label">{selectLabel}</p> : null}
				<div className="select_body" onClick={handleClickSelectBody}>
					{nowValue}
				</div>
				<ul className="option_list">
					{children
						? children
						: selectItem
						? selectItem.map((value, idx) => (
								<li
									key={idx}
									onClick={(event) => {
										handleClickListItem(event);
									}}
								>
									{value}
								</li>
						  ))
						: null}
				</ul>
			</div>
		</div>
	);
};
export default SelectBox;
