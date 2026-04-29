import request from '@/utils/request'

export const login = (data) => {
    return request.post('/user/login', data)
}

export const register = (data) => {
    return request.post('/user/register', data)
}

export const getUserInfo = () => {
    return request.get('/user/profile')
}

export const updateUserInfo = (data) => {
    return request.put('/user/profile', data)
}