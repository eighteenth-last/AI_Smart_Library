import { defineStore } from 'pinia';
import { ref } from 'vue';

export interface UserInfo {
  userId: number;
  username: string;
  nickname: string;
  role: 'reader' | 'admin' | 'author';
  email: string;
  phone?: string;
  profilePicture?: string;
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '');
  
  // 从 localStorage 读取用户信息
  const savedUserInfo = localStorage.getItem('userInfo');
  const userInfo = ref<UserInfo | null>(savedUserInfo ? JSON.parse(savedUserInfo) : null);

  const setToken = (newToken: string) => {
    token.value = newToken;
    localStorage.setItem('token', newToken);
  };

  const setUserInfo = (info: UserInfo) => {
    userInfo.value = info;
    // 保存到 localStorage
    localStorage.setItem('userInfo', JSON.stringify(info));
  };

  // setUser 别名，与 setUserInfo 相同
  const setUser = (info: UserInfo) => {
    userInfo.value = info;
    // 保存到 localStorage
    localStorage.setItem('userInfo', JSON.stringify(info));
  };

  const logout = () => {
    token.value = '';
    userInfo.value = null;
    localStorage.removeItem('token');
    localStorage.removeItem('userInfo');
  };

  // clearUser 别名，与 logout 相同
  const clearUser = () => {
    logout();
  };

  const isLoggedIn = () => {
    return !!token.value;
  };

  const hasRole = (role: string) => {
    return userInfo.value?.role === role;
  };

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    setUser,
    logout,
    clearUser,
    isLoggedIn,
    hasRole,
  };
});
