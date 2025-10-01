package com.rentmate.service.user.service.implementation;

import com.rentmate.service.user.service.shared.specification.UserSpecification;
import com.rentmate.service.user.domain.dto.event.ProfileDisabledEvent;
import com.rentmate.service.user.domain.dto.event.UserRegisteredEvent;
import com.rentmate.service.user.domain.dto.user.*;
import com.rentmate.service.user.domain.entity.User;
import com.rentmate.service.user.domain.enumuration.AccountActivityStatus;
import com.rentmate.service.user.domain.enumuration.UserRole;
import com.rentmate.service.user.domain.mapper.EventMapper;
import com.rentmate.service.user.domain.mapper.UserMapper;
import com.rentmate.service.user.repository.UserRepository;
import com.rentmate.service.user.repository.UserSessionRepository;
import com.rentmate.service.user.service.UserEventPublisher;
import com.rentmate.service.user.service.UserService;
import com.rentmate.service.user.service.shared.exception.BadRequestException;
import com.rentmate.service.user.service.shared.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service @RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserSessionRepository userSessionRepository;
    private final UserEventPublisher eventPublisher;

    @Override
    public UserProfileResponse getUserProfile() {
        User user = userRepository.
                findById(UserService.getAuthenticatedUserId()).
                orElseThrow(() -> new NotFoundException("User profile not found"));

        return UserMapper.toUserProfileResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(UpdateUserProfileRequest request) {
        User user = userRepository.
                findById(UserService.getAuthenticatedUserId()).
                orElseThrow(() -> new NotFoundException("User profile not found"));

        if(!user.getPhoneNumber().equals(request.phoneNumber()) &&
                userRepository.userExists(null, request.phoneNumber()).orElse(false)) {
            throw new BadRequestException("User with this phone number already exists");
        }

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhoneNumber(request.phoneNumber().trim());

        userRepository.save(user);

        return UserMapper.toUserProfileResponse(user);
    }

    @Override
    public PublicUserProfileResponse getPublicUserProfile(Long userId) {
        return userRepository.findById(userId, PublicUserProfileResponse.class)
                .orElseThrow(() -> new NotFoundException("User profile not found"));
    }

    //TODO: check if user have an active rentals.
    @Override @Transactional
    public void disableOwnProfile() {
        Long userId = UserService.getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User profile not found"));
        user.setDisabled(true);
        user.setActivityStatus(AccountActivityStatus.INACTIVE);
        userRepository.save(user);

        userSessionRepository.deactivateSessionsForUser(userId);

        ProfileDisabledEvent event = EventMapper.toProfileDisabledEvent(user,"self desire", "SELF");
        eventPublisher.publishProfileDisabledEvent(event);
    }

    @Override @Transactional
    public UserProfileResponse updateProfileStatus(Long userId, UpdateProfileStatusRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User profile not found"));

        user.setActivityStatus(request.status());
        if(request.status() == AccountActivityStatus.SUSPENDED_BY_ADMIN) {
            user.setDisabled(true);
            userSessionRepository.deactivateSessionsForUser(userId);
        }

        userRepository.save(user);
        userRepository.flush();

        if (user.isDisabled()) {
            ProfileDisabledEvent event = EventMapper.toProfileDisabledEvent(user, request.reason(), "ADMIN");
            eventPublisher.publishProfileDisabledEvent(event);
        }

        return UserMapper.toUserProfileResponse(user);
    }

    @Override
    public UserProfileResponse createProfile(CreateProfileRequest request) {
        User user = UserMapper.toUser(request);
        userRepository.save(user);
        userRepository.flush();

        UserRegisteredEvent event = EventMapper.toUserRegisteredEvent(user);
        eventPublisher.publishUserRegistered(event);

        return UserMapper.toUserProfileResponse(user);
    }

    @Override @Transactional
    public UserProfileResponse updateProfileRole(Long userId, UpdateProfileRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User profile not found"));

        user.setRole(request.role());
        userRepository.save(user);

        return UserMapper.toUserProfileResponse(user);
    }

    @Override
    public UserListResponse getAllUsers(Integer page, Integer limit, UserRole role, AccountActivityStatus status,
                                        Boolean isVerified, String search, String sortBy, String sortOrder) {

        Specification<User> spec = Specification.unrestricted();

        if (role != null) spec = spec.and(UserSpecification.hasRole(role));
        if (status != null) spec = spec.and(UserSpecification.hasStatus(status));
        if (isVerified != null) spec = spec.and(UserSpecification.isVerified(isVerified));
        if (search != null && !search.trim().isEmpty() && search.length() >= 2)
            spec = spec.and(UserSpecification.searchByKeyword(search.trim()));


        Sort sort = createSort(sortBy, sortOrder);
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<User> userPage = userRepository.findAll(spec, pageable);


        List<UserProfileResponse> userProfiles = userPage.getContent().stream()
                .map(UserMapper::toUserProfileResponse)
                .toList();

        return new UserListResponse(
                userProfiles,
                page,
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                limit,
                userPage.hasNext(),
                userPage.hasPrevious()
        );
    }


    private Sort createSort(String sortBy, String sortOrder) {
        String field = mapSortField(sortBy);

        Sort.Direction direction = "asc".equalsIgnoreCase(sortOrder)
                ? Sort.Direction.ASC : Sort.Direction.DESC;

        return Sort.by(direction, field);
    }

    private String mapSortField(String sortBy) {
        sortBy = sortBy.toLowerCase().trim();
        switch (sortBy) {
            case "createdat":
                return "createdAt";
            case "updatedat":
                return "updatedAt";
            case "averagerating":
                return "averageRating";
            case "firstname":
                return "firstName";
            default:
                return "createdAt";
        }
    }
}
