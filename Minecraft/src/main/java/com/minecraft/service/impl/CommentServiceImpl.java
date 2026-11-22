package com.minecraft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minecraft.dto.request.CommentRequest;
import com.minecraft.dto.request.PageRequest;
import com.minecraft.dto.response.PageResponse;
import com.minecraft.entity.Comment;
import com.minecraft.mapper.CommentMapper;
import com.minecraft.service.CommentService;
import com.minecraft.vo.CommentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    /** 单次评论树最多加载条数，避免热门内容评论无限增长 */
    private static final int MAX_TREE_COMMENTS = 1000;

    @Override
    public void addComment(CommentRequest request, Long userId) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setItemType(request.getItemType());
        comment.setItemId(request.getItemId());
        comment.setParentId(request.getParentId());
        comment.setContent(request.getContent());
        comment.setImages(request.getImages());
        comment.setRating(request.getRating());
        save(comment);
    }

    @Override
    public void deleteComment(Long id) {
        removeById(id);
    }

    @Override
    public PageResponse<CommentVO> getCommentList(String itemType, Long itemId, PageRequest request) {
        Page<Comment> page = new Page<>(request.getPageNum(), request.getPageSize());
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getItemType, itemType)
                .eq(Comment::getItemId, itemId)
                .isNull(Comment::getParentId)
                .orderByDesc(Comment::getCreateTime);

        Page<Comment> result = page(page, wrapper);

        List<CommentVO> voList = result.getRecords().stream().map(item -> {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(item, vo);
            return vo;
        }).collect(Collectors.toList());

        return new PageResponse<>(voList, result.getTotal(), request.getPageNum(), request.getPageSize());
    }

    @Override
    public List<CommentVO> getCommentTree(String itemType, Long itemId) {
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getItemType, itemType)
                .eq(Comment::getItemId, itemId)
                .orderByDesc(Comment::getCreateTime)
                .last("LIMIT " + MAX_TREE_COMMENTS);

        List<Comment> allComments = list(wrapper);
        Map<Long, List<Comment>> childrenByParent = new HashMap<>();
        List<Comment> rootComments = new ArrayList<>();
        for (Comment comment : allComments) {
            if (comment.getParentId() == null) {
                rootComments.add(comment);
            } else {
                childrenByParent.computeIfAbsent(comment.getParentId(), key -> new ArrayList<>()).add(comment);
            }
        }

        List<CommentVO> result = new ArrayList<>(rootComments.size());
        for (Comment root : rootComments) {
            CommentVO vo = toVO(root);
            vo.setReplies(new ArrayList<>());
            appendChildren(vo, childrenByParent);
            result.add(vo);
        }
        return result;
    }

    /**
     * 基于 parentId 分组挂载子评论，整体 O(n)，替代原先对全量列表的 O(n²) 递归扫描。
     */
    private void appendChildren(CommentVO parent, Map<Long, List<Comment>> childrenByParent) {
        for (Comment child : childrenByParent.getOrDefault(parent.getId(), List.of())) {
            CommentVO childVO = toVO(child);
            childVO.setReplies(new ArrayList<>());
            parent.getReplies().add(childVO);
            appendChildren(childVO, childrenByParent);
        }
    }

    private CommentVO toVO(Comment comment) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(comment, vo);
        return vo;
    }
}
