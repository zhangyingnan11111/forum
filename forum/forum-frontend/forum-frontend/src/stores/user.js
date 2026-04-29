import { defineStore } from 'pinia'
import { login, register, getUserInfo, updateUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
    state: () => ({
        token: localStorage.getItem('token') || '',
        userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
    }),

    getters: {
        isLoggedIn: (state) => !!state.token,
        isAdmin: (state) => state.userInfo.role === 'admin',
        isModerator: (state) => state.userInfo.role === 'moderator'
    },

    actions: {
        async login(credentials) {
            const res = await login(credentials)
            if (res.code === 200) {
                this.token = res.data.token
                this.userInfo = {
                    userId: res.data.userId,
                    username: res.data.username,
                    email: res.data.email,
                    avatar: res.data.avatar,
                    role: res.data.role
                }
                localStorage.setItem('token', res.data.token)
                localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
                return true
            }
            return false
        },

        async register(userData) {
            const res = await register(userData)
            return res.code === 200
        },

        async fetchUserInfo() {
            const res = await getUserInfo()
            if (res.code === 200) {
                this.userInfo = { ...this.userInfo, ...res.data }
                localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
            }
        },

        async updateProfile(data) {
            const res = await updateUserInfo(data)
            if (res.code === 200) {
                this.userInfo = { ...this.userInfo, ...res.data }
                localStorage.setItem('userInfo', JSON.stringify(this.userInfo))
                return true
            }
            return false
        },

        logout() {
            this.token = ''
            this.userInfo = {}
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
        }
    }
})