import request from '@/utils/request'

export const getPostList = (params) => {
    return request.get('/post/list', { params })
}

export const getPostDetail = (id) => {
    return request.get(`/post/${id}`)
}

export const createPost = (data) => {
    return request.post('/post/create', data)
}

export const updatePost = (id, data) => {
    return request.put(`/post/update/${id}`, data)
}

export const deletePost = (id) => {
    return request.delete(`/post/delete/${id}`)
}

export const topPost = (id, isTop) => {
    return request.put(`/post/top/${id}`, null, { params: { isTop } })
}

export const essencePost = (id, isEssence) => {
    return request.put(`/post/essence/${id}`, null, { params: { isEssence } })
}

export const searchPosts = (params) => {
    return request.get('/post/search', { params })
}

export const getMyPosts = (params) => {
    return request.get('/post/my-posts', { params })
}

export const getCategories = () => {
    return request.get('/post/categories')
}