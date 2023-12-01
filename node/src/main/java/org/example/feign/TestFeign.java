package org.example.feign;

import feign.Response;
import org.example.pojo.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.util.Map;
import java.util.Set;

@FeignClient(name = "node01-service", url = "EMPTY")
public interface TestFeign {

    @GetMapping("/bucket/test")
    String test(URI uri, @RequestParam String str);

    @PostMapping("/bucket/delete_bucket")
    ResponseResult deleteBucket(URI uri, @RequestBody Map<String, String> parameters);

    @PostMapping(value = "/get/getFile")
    Response getFile(URI uri,
                     @RequestParam("bucketId") String bucketId,
                     @RequestParam("fileName") String fileName,
                     @RequestParam(value = "version", required = false) Integer version,
                     @RequestParam boolean isBase64,
                     @RequestParam("isInternal") Integer isInternal);

    @GetMapping("/get/getBucket/{bucketId}/{isInternal}")
    ResponseResult<Set<String>> getBucket(URI uri, @PathVariable String bucketId,
                                          @PathVariable Integer isInternal);

    @RequestMapping(value = "/put/uploadSimple",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseResult<String> uploadSimple(URI uri,
                                        @RequestPart("file") MultipartFile file,
                                        @RequestParam("originMd5") String originMd5,
                                        @RequestParam("bucketId") String bucketId,
                                        @RequestParam("isZip") Boolean isZip,
                                        @RequestParam Integer isInternal);

    @PostMapping(value = "/put/shardPreparation")
    ResponseResult shardPreparation(URI uri,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("shardNum") Integer shardNum,
                                    @RequestParam("shardSize") Long shardSize,
                                    @RequestParam("bucketId") String bucketId,
                                    @RequestParam("isZip") Boolean isZip,
                                    @RequestParam("originMd5") String originMd5,
                                    @RequestParam Integer isInternal);

    @RequestMapping(value = "/put/uploadShard",
            method = RequestMethod.POST,
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseResult<Integer> uploadShard(URI uri,
                                        @RequestParam("no") Integer no,
                                        @RequestParam("totalMd5") String totalMd5,
                                        @RequestParam("ownMd5") String ownMd5,
                                        @RequestPart("file") MultipartFile file,
                                        @RequestParam("key") String key,
                                        @RequestParam Integer isInternal);

    @PostMapping(value = "/put/checkShard")
    ResponseResult<Set<Integer>> checkShard(URI uri,
                                            @RequestParam("md5") String md5,
                                            @RequestParam("key") String key,
                                            @RequestParam Integer isInternal);

    @PostMapping(value = "/delete/delFile")
    ResponseResult delFile(URI uri,
                           @RequestParam String bucketId,
                           @RequestParam String fileName,
                           @RequestParam Boolean isForever,
                           @RequestParam Integer isInternal);

    @RequestMapping(value = "/delete/recoverFile", method = RequestMethod.POST)
    ResponseResult recoverFile(URI uri,
                               @RequestParam String bucketId,
                               @RequestParam String fileName,
                               @RequestParam Integer isInternal);

}
