package com.paladin.vod.web;

import com.paladin.framework.common.R;
import com.paladin.vod.model.UploadFile;
import com.paladin.vod.service.FileUploader;
import com.paladin.vod.service.UploadFileService;
import com.paladin.vod.service.dto.UploadFileDTO;
import com.paladin.vod.service.dto.UploadFileQuery;
import com.paladin.vod.web.dto.UploadChunk;
import com.paladin.vod.web.dto.UploadStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.HashMap;

@Controller
@RequestMapping("/vod/upload")
public class UploadFileController {

    @Autowired
    private UploadFileService uploadFileService;

    @Value("${vod.rtmp-server-url}")
    private String rtmpServerUrl;

    @GetMapping(value = "/index")
    public Object index() {
        return "/index";
    }

    @PostMapping("/create")
    @ResponseBody
    public R beginUpload(@Valid UploadFileDTO uploadFileDTO) {
        UploadFile uploadFile = uploadFileService.createUploadFile(uploadFileDTO);
        FileUploader uploader = uploadFileService.getOrCreateUploader(uploadFile.getId());
        return R.success(getUploadStatus(uploader));
    }

    @PostMapping("/continue")
    @ResponseBody
    public R continueUpload(@RequestParam("id") String id) {
        FileUploader uploader = uploadFileService.getOrCreateUploader(id);
        return R.success(getUploadStatus(uploader));
    }

    @PostMapping("/chunk")
    @ResponseBody
    public R uploadChunk(UploadChunk uploadChunk) {
        try {
            FileUploader uploader = uploadFileService.uploadFileChunk(uploadChunk.getId(), uploadChunk.getChunk(), uploadChunk.getFile().getInputStream());
            return R.success(getUploadStatus(uploader));
        } catch (IOException e) {
            return R.fail("上传文件异常", e);
        }
    }

    private UploadStatus getUploadStatus(FileUploader uploader) {
        UploadStatus status = new UploadStatus();

        status.setId(uploader.getId());
        status.setChunkSize(uploader.getChunkSize());
        status.setCurrentChunk(uploader.getCurrentChunk());

        if (uploader.isCompleted()) {
            status.setStatus(UploadFile.STATUS_COMPLETED);
        } else {
            status.setStatus(UploadFile.STATUS_UPLOADING);
        }

        return status;
    }

    @GetMapping(value = "/find/uploading")
    @ResponseBody
    public R findUploadingFiles() {
        return R.success(uploadFileService.findUploadingFile());
    }

    @GetMapping(value = "/find/completed/page")
    @ResponseBody
    public R findCompletedFiles(UploadFileQuery query) {
        HashMap<String, Object> result = new HashMap<>();
        result.put("url", rtmpServerUrl);
        result.put("list", uploadFileService.findCompletedFile(query));
        return R.success(result);
    }

    @PostMapping(value = "/delete")
    @ResponseBody
    public R deleteFile(@RequestParam("id") String id) {
        uploadFileService.removeUploadFile(id);
        return R.success();
    }

    @GetMapping(value = "/clean")
    @ResponseBody
    public R cleanFile() {
        uploadFileService.cleanUploadFile();
        return R.success();
    }


}
