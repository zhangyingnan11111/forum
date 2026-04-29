import request from '@/utils/request'

export const createComment = (data) => {
    return request.post('/comment/create', data)
}

export const deleteComment = (id) => {
    return request.delete(`/comment/delete/${id}`)
}

export const getComments = (postId) => {
    return request.get(`/comment/list/${postId}`)
}