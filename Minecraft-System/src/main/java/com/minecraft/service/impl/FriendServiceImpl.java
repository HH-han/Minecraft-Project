package com.minecraft.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minecraft.dto.response.FriendInfoDTO;
import com.minecraft.dto.response.FriendRequestDTO;
import com.minecraft.entity.Friend;
import com.minecraft.entity.User;
import com.minecraft.constant.CacheConstants;
import com.minecraft.enums.FriendStatus;
import com.minecraft.exception.BusinessException;
import com.minecraft.mapper.FriendMapper;
import com.minecraft.service.FriendService;
import com.minecraft.service.UserService;
import com.minecraft.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements FriendService {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisUtil redisUtil;

    private static final String UNREAD_COUNT_KEY = "chat:unread:";

    @Override
    public void addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new BusinessException("不能添加自己为好友");
        }

        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
        if (getOne(wrapper) != null) {
            throw new BusinessException("已经是好友或已发送好友请求");
        }

        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus(FriendStatus.PENDING.ordinal());
        save(friend);
    }

    @Override
    public void acceptFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, friendId)
                .eq(Friend::getFriendId, userId);
        Friend friend = getOne(wrapper);
        if (friend == null) {
            throw new BusinessException("好友请求不存在");
        }
        friend.setStatus(FriendStatus.ACCEPTED.ordinal());
        updateById(friend);

        Friend reverseFriend = new Friend();
        reverseFriend.setUserId(userId);
        reverseFriend.setFriendId(friendId);
        reverseFriend.setStatus(FriendStatus.ACCEPTED.ordinal());
        save(reverseFriend);
    }

    @Override
    public void deleteFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId))
                .or()
                .eq(Friend::getUserId, friendId)
                .eq(Friend::getFriendId, userId);
        remove(wrapper);
    }

    @Override
    public void updateRemark(Long userId, Long friendId, String remark) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getFriendId, friendId);
        Friend friend = getOne(wrapper);
        if (friend != null) {
            friend.setRemark(remark);
            updateById(friend);
        }
    }

    @Override
    public List<Friend> getFriendList(Long userId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getUserId, userId)
                .eq(Friend::getStatus, FriendStatus.ACCEPTED.ordinal());
        return list(wrapper);
    }

    @Override
    public List<FriendInfoDTO> getFriendInfoList(Long userId) {
        List<Friend> friends = getFriendList(userId);
        if (friends.isEmpty()) {
            return new ArrayList<>();
        }

        // 批量查用户，避免每个好友一次 selectById
        List<Long> friendIds = friends.stream().map(Friend::getFriendId).distinct().toList();
        java.util.Map<Long, User> userMap = userService.listByIds(friendIds).stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, user -> user));

        // 批量取在线状态与未读数，避免 2N 次 Redis 往返
        List<String> onlineKeys = friendIds.stream()
                .map(id -> CacheConstants.ONLINE_STATUS_KEY + id).toList();
        List<String> unreadKeys = friendIds.stream()
                .map(id -> UNREAD_COUNT_KEY + userId + ":" + id).toList();
        List<Object> onlineValues = redisUtil.multiGet(onlineKeys);
        List<Object> unreadValues = redisUtil.multiGet(unreadKeys);
        java.util.Map<Long, Integer> indexMap = new java.util.HashMap<>(friendIds.size());
        for (int i = 0; i < friendIds.size(); i++) {
            indexMap.put(friendIds.get(i), i);
        }

        List<FriendInfoDTO> friendInfoList = new ArrayList<>(friends.size());
        for (Friend friend : friends) {
            FriendInfoDTO dto = new FriendInfoDTO();
            dto.setFriendId(friend.getFriendId());
            dto.setRemark(friend.getRemark());

            User user = userMap.get(friend.getFriendId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setAvatar(user.getAvatar());
                dto.setPhone(user.getPhone());
                dto.setEmail(user.getEmail());
            }

            Integer index = indexMap.get(friend.getFriendId());
            boolean online = index != null && onlineValues != null && index < onlineValues.size()
                    && "1".equals(String.valueOf(onlineValues.get(index)));
            dto.setOnline(online);
            Object unreadCount = index != null && unreadValues != null && index < unreadValues.size()
                    ? unreadValues.get(index) : null;
            dto.setUnreadCount(unreadCount instanceof Number number ? number.intValue() : 0);

            friendInfoList.add(dto);
        }

        return friendInfoList;
    }

    @Override
    public List<Friend> getFriendRequestList(Long userId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friend::getFriendId, userId)
                .eq(Friend::getStatus, FriendStatus.PENDING.ordinal());
        return list(wrapper);
    }

    @Override
    public boolean isFriend(Long userId, Long friendId) {
        LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Friend::getUserId, userId).eq(Friend::getFriendId, friendId))
                .or()
                .eq(Friend::getUserId, friendId)
                .eq(Friend::getFriendId, userId);
        wrapper.eq(Friend::getStatus, FriendStatus.ACCEPTED.ordinal());
        return count(wrapper) > 0;
    }

    @Override
    public List<FriendRequestDTO> getFriendRequestInfoList(Long userId) {
        List<Friend> requests = getFriendRequestList(userId);
        if (requests.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> requestUserIds = requests.stream().map(Friend::getUserId).distinct().toList();
        java.util.Map<Long, User> userMap = userService.listByIds(requestUserIds).stream()
                .collect(java.util.stream.Collectors.toMap(User::getId, user -> user));

        List<FriendRequestDTO> requestInfoList = new ArrayList<>(requests.size());
        for (Friend request : requests) {
            FriendRequestDTO dto = new FriendRequestDTO();
            dto.setId(request.getId());
            dto.setUserId(request.getUserId());
            dto.setFriendId(request.getFriendId());
            dto.setMessage(request.getRemark());
            dto.setCreateTime(request.getCreateTime() != null ? request.getCreateTime().toString() : null);

            User user = userMap.get(request.getUserId());
            if (user != null) {
                dto.setUsername(user.getUsername());
                dto.setAvatar(user.getAvatar());
                dto.setPhone(user.getPhone());
                dto.setEmail(user.getEmail());
            }

            requestInfoList.add(dto);
        }

        return requestInfoList;
    }
}