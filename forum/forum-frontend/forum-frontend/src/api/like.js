import request from '@/utils/request'

export const like = (targetType, targetId) => {
    return request.post(`/like/${targetType}/${targetId}`)
}

export const unlike = (targetType, targetId) => {
    return request.delete(`/like/${targetType}/${targetId}`)
}

export const getLikeStatus = (targetType, targetId) => {
    return request.get(`/like/status/${targetType}/${targetId}`)
}