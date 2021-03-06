package com.wiz.controller;

import com.wiz.annotation.LoginRequired;
import com.wiz.entity.User;
import com.wiz.service.FollowService;
import com.wiz.service.LIkeService;
import com.wiz.service.UserService;
import com.wiz.util.CommunityConstant;
import com.wiz.util.CommunityUtil;
import com.wiz.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.codec.multipart.PartHttpMessageWriter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * @Description:
 * @Create: 2022-03-20-21:41
 * @Author: Hey
 */

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LIkeService lIkeService;

    @Autowired
    private FollowService followService;

    @LoginRequired
    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public String getSettingPage() {
        return "site/setting";
    }

    @LoginRequired
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "????????????????????????");
            return "site/setting";
        }

        String filename = headerImage.getOriginalFilename();
        String suffix = filename.substring(filename.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "????????????????????????");
            return "site/setting";
        }

        // ?????????????????????
        filename = CommunityUtil.generateUUID() + suffix;
        // ???????????????????????????
        File dest = new File(uploadPath + "/" + filename);
        try {
            // ????????????
            headerImage.transferTo(dest);
        } catch (IOException e) {
            LOGGER.error("?????????????????????" + e.getMessage());
            throw new RuntimeException("??????????????????????????????????????????", e);
        }

        // ???????????????????????????????????????web???????????????
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + filename;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }

    @RequestMapping(value = "/header/{filename}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String filename, HttpServletResponse response) {

        // ?????????????????????
        filename = uploadPath + "/" + filename;
        //???????????????
        String suffix = filename.substring(filename.lastIndexOf("."));
        // ????????????
        response.setContentType("image/" + suffix);
        try (
                OutputStream os = response.getOutputStream();
                FileInputStream fis = new FileInputStream(filename);
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            LOGGER.error("?????????????????????" + e.getMessage());
        }

    }

    //????????????
    @RequestMapping(path = "/password",method = RequestMethod.POST)
    public String updatePassword(String oldPassword, String newPassword,Model model) {
        User user = hostHolder.getUser();
        Map<String, Object> map = userService.updatePassword(user.getId(), oldPassword, newPassword);
        if (map.containsKey("passwordMsg")) {
            model.addAttribute("passwordMsg", map.get("passwordMsg"));
            return "site/setting";
        }
        return "redirect:/index";
    }

    // ????????????
    @RequestMapping(path = "/profile/{userId}", method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model) {
        User user = userService.findUserById(userId);
        if (user == null) {
            throw new RuntimeException("??????????????????");
        }

        // ??????
        model.addAttribute("user",user);
        // ????????????
        int likeCount = lIkeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        // ????????????
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // ????????????
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // ???????????????
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);
        return "site/profile";
    }

}



















