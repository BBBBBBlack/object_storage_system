package org.example.controller;


import org.example.service.DeleteService;
import org.example.pojo.ResponseResult;
import org.example.service.ClusterDeleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delete")
public class DeleteController {
    @Autowired
    private DeleteService deleteService;

    @Autowired
    private ClusterDeleteService cDeleteService;

    @PostMapping("/delFile")
    public ResponseResult delFile(@RequestParam String bucketId,
                                  @RequestParam String fileName,
                                  @RequestParam Boolean isForever,
                                  @RequestParam Integer isInternal) {
        if (isInternal == 1) {
            return deleteService.delFile(bucketId, fileName, isForever);
        } else {
            return cDeleteService.delFile(bucketId, fileName, isForever);
        }
    }

    @PostMapping("/recoverFile")
    public ResponseResult recoverFile(@RequestParam String bucketId,
                              @RequestParam String fileName,
                              @RequestParam Integer isInternal) {
        if (isInternal == 1) {
            return deleteService.recoverFile(bucketId, fileName);
        } else {
            return cDeleteService.recoverFile(bucketId, fileName);
        }
    }

//    @PostMapping("/delBucket")
//    public void delBucket(@RequestParam String bucketId) {
//        cDeleteService.delBucket(bucketId);
//    }
}
