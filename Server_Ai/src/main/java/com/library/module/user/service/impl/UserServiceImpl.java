package com.library.module.user.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.library.common.exception.BusinessException;
import com.library.common.result.PageResult;
import com.library.common.utils.PasswordUtil;
import com.library.module.author.entity.Author;
import com.library.module.author.mapper.AuthorMapper;
import com.library.module.user.dto.UserLoginDTO;
import com.library.module.user.dto.UserRegisterDTO;
import com.library.module.user.dto.UserUpdateDTO;
import com.library.module.user.entity.User;
import com.library.module.user.mapper.UserMapper;
import com.library.module.user.service.UserService;
import com.library.module.user.vo.LoginVO;
import com.library.module.user.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    
    @Autowired(required = false)
    private AuthorMapper authorMapper;

    public UserServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public LoginVO register(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, registerDTO.getEmail());
        existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPasswordHash(PasswordUtil.hashPassword(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setRole(registerDTO.getRole());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setStatus(1); // 默认启用
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        log.info("用户注册成功：{}, 角色：{}", registerDTO.getUsername(), registerDTO.getRole());

        // 如果注册的是作者角色，同时创建author记录
        if ("author".equals(registerDTO.getRole()) && authorMapper != null) {
            Author author = new Author();
            author.setUserId(user.getUserId());
            // 使用用户名作为默认作者名
            author.setName(registerDTO.getNickname() != null ? registerDTO.getNickname() : registerDTO.getUsername());
            author.setAuthStatus(0); // 0-待审核
            author.setDeleted(0);
            
            authorMapper.insert(author);
            log.info("自动创建作者记录：{}, 等待审核", author.getName());
        }

        // 注意：作者注册后不自动登录，需要等待管理员审核
        if ("author".equals(registerDTO.getRole())) {
            LoginVO loginVO = new LoginVO();
            loginVO.setUserId(user.getUserId());
            loginVO.setUsername(user.getUsername());
            loginVO.setNickname(user.getNickname());
            loginVO.setRole(user.getRole());
            loginVO.setEmail(user.getEmail());
            loginVO.setPhone(user.getPhone());
            loginVO.setToken(null); // 作者注册后不返图token
            loginVO.setExpiresIn(0L);
            return loginVO;
        }

        // 普通用户自动登录
        return login(new UserLoginDTO(registerDTO.getUsername(), registerDTO.getPassword()));
    }

    @Override
    public LoginVO login(UserLoginDTO loginDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        if (user.getStatus() == 0) {
            throw new BusinessException("账户已被禁用");
        }

        if (!PasswordUtil.verifyPassword(loginDTO.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("密码错误");
        }

        // 如果是作者角色，检查认证状态
        if ("author".equals(user.getRole()) && authorMapper != null) {
            LambdaQueryWrapper<Author> authorWrapper = new LambdaQueryWrapper<>();
            authorWrapper.eq(Author::getUserId, user.getUserId())
                         .eq(Author::getDeleted, 0);
            Author author = authorMapper.selectOne(authorWrapper);
            
            if (author == null) {
                throw new BusinessException("作者信息不存在，请先注册");
            }
            
            if (author.getAuthStatus() == 0) {
                throw new BusinessException("作者认证待审核，请耐心等待管理员审核");
            }
            
            if (author.getAuthStatus() == 2) {
                throw new BusinessException("作者认证已被拒绝，请联系管理员");
            }
        }

        // 生成Token
        StpUtil.login(user.getUserId());
        String token = StpUtil.getTokenValue();

        // 构建返回对象
        LoginVO loginVO = new LoginVO();
        loginVO.setUserId(user.getUserId());
        loginVO.setUsername(user.getUsername());
        loginVO.setNickname(user.getNickname());
        loginVO.setRole(user.getRole());
        loginVO.setEmail(user.getEmail());
        loginVO.setPhone(user.getPhone());
        loginVO.setProfilePicture(user.getProfilePicture());
        loginVO.setToken(token);
        loginVO.setExpiresIn(2592000L); // 30天

        return loginVO;
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public UserVO getCurrentUser() {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public void updateProfile(UserUpdateDTO updateDTO) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查用户名是否被其他用户使用
        if (updateDTO.getUsername() != null && !updateDTO.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, updateDTO.getUsername())
                   .ne(User::getUserId, userId);
            User existUser = userMapper.selectOne(wrapper);
            if (existUser != null) {
                throw new BusinessException("用户名已被其他用户使用");
            }
        }

        // 检查邮箱是否被其他用户使用
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, updateDTO.getEmail())
                   .ne(User::getUserId, userId);
            User existUser = userMapper.selectOne(wrapper);
            if (existUser != null) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
        }

        // 更新用户信息
        if (updateDTO.getUsername() != null) {
            user.setUsername(updateDTO.getUsername());
        }
        if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
            user.setPasswordHash(PasswordUtil.hashPassword(updateDTO.getPassword()));
        }
        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getEmail() != null) {
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getProfilePicture() != null) {
            user.setProfilePicture(updateDTO.getProfilePicture());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户 {} 更新个人信息", userId);
    }

    @Override
    public void updateAvatar(String avatarUrl) {
        Long userId = StpUtil.getLoginIdAsLong();
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        user.setProfilePicture(avatarUrl);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("用户 {} 更新头像: {}", userId, avatarUrl);
    }

     @Override
    public PageResult<UserVO> getUserList(String keyword, String role, Integer status, Long page, Long size) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                             .or()
                             .like(User::getNickname, keyword));
        }
        
        if (role != null) {
            wrapper.eq(User::getRole, role);
        }
        
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        
        wrapper.orderByDesc(User::getUserId);

        Page<User> pageParam = new Page<>(page, size);
        Page<User> userPage = userMapper.selectPage(pageParam, wrapper);

        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(user -> {
                    UserVO userVO = new UserVO();
                    BeanUtils.copyProperties(user, userVO);
                    return userVO;
                })
                .collect(Collectors.toList());

        return PageResult.of(userVOList, userPage.getTotal(), userPage.getCurrent(), userPage.getSize());
    }

    @Override
    public void addUser(UserRegisterDTO registerDTO) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, registerDTO.getUsername());
        User existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getEmail, registerDTO.getEmail());
        existUser = userMapper.selectOne(wrapper);
        if (existUser != null) {
            throw new BusinessException("邮箱已被注册");
        }

        // 创建用户
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPasswordHash(PasswordUtil.hashPassword(registerDTO.getPassword()));
        user.setNickname(registerDTO.getNickname());
        user.setRole(registerDTO.getRole());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setStatus(registerDTO.getStatus() != null ? registerDTO.getStatus() : 1);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userMapper.insert(user);
        log.info("管理员新增用户：{}", registerDTO.getUsername());
    }

    @Override
    public void updateUser(Long userId, UserUpdateDTO updateDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 检查邮箱是否被其他用户使用
        if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getEmail, updateDTO.getEmail())
                   .ne(User::getUserId, userId);
            User existUser = userMapper.selectOne(wrapper);
            if (existUser != null) {
                throw new BusinessException("邮箱已被其他用户使用");
            }
        }

        // 更新用户信息
        if (updateDTO.getNickname() != null) {
            user.setNickname(updateDTO.getNickname());
        }
        if (updateDTO.getPhone() != null) {
            user.setPhone(updateDTO.getPhone());
        }
        if (updateDTO.getEmail() != null) {
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getStatus() != null) {
            user.setStatus(updateDTO.getStatus());
        }

        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        log.info("管理员更新用户 {} 的信息", userId);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // 防止删除管理员自己
        if (userId.equals(StpUtil.getLoginIdAsLong())) {
            throw new BusinessException("不能删除自己的账户");
        }

        userMapper.deleteById(userId);
        log.info("管理员删除用户：{}", userId);
    }
}