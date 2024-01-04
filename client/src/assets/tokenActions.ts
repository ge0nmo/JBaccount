export const isAccessToken = () => {
	const token = localStorage.getItem("accessToken");
	return !!token;
};
export const saveAccessToken = (token: string) => {
	localStorage.setItem("accessToken", token);
};
export const removeAccessToken = () => {
	localStorage.removeItem("accessToken");
};
