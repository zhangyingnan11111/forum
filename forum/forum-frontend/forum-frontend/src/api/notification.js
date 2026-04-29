import request from '@/utils/request'

export const getNotifications = (params) => {
    return request.get('/notification/list', { params })
}

export const getUnreadCount = () => {
    return request.get('/notification/unread/count')
}

export const markAsRead = (id) => {
    return request.put(`/notification/read/${id}`)
}

export const markAllAsRead = () => {
    return request.put('/notification/read/all')
}