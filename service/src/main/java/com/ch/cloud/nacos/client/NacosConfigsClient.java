package com.ch.cloud.nacos.client;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ch.cloud.nacos.NacosAPI;
import com.ch.cloud.nacos.dto.ConfigDTO;
import com.ch.cloud.nacos.vo.*;
import com.ch.cloud.utils.ContextUtil;
import com.ch.e.PubError;
import com.ch.result.InvokerPage;
import com.ch.utils.AssertUtils;
import com.ch.utils.BeanUtilsV2;
import com.ch.utils.CommonUtils;
import com.ch.utils.FileUtilsV2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * desc:
 *
 * @author zhimin
 * @since 2022/4/25 23:31
 */
@Component
@Slf4j
public class NacosConfigsClient extends BaseClient {

    @Value("${fs.path.tmp:/tmp}")
    private String fsTmp;

    public InvokerPage.Page<ConfigDTO> fetchPage(ClientEntity<ConfigsPageVO> clientEntity) {
        // String urlParams = urlParams(clientEntity.getData());
        String url = urlWithData(NacosAPI.CONFIGS, clientEntity);
        log.info("nacos configs page url: {}", url);
        JSONObject resp = invoke(url, HttpMethod.GET, HttpEntity.EMPTY, JSONObject.class);
        if (resp != null && resp.containsKey("totalCount")) {
            Integer count = resp.getInteger("totalCount");
            if (count <= 0) {
                return InvokerPage.build();
            }
            JSONArray arr = resp.getJSONArray("pageItems");
            List<ConfigDTO> records = arr.toJavaList(ConfigDTO.class);
            return InvokerPage.build(count, records);
        }
        return InvokerPage.build();
    }

    public Boolean add(ClientEntity<ConfigVO> entity) {
        return save(entity, true);
    }

    public Boolean edit(ClientEntity<ConfigVO> entity) {
        return save(entity, false);
    }

    private Boolean save(ClientEntity<ConfigVO> clientEntity, boolean isNew) {
        if (isNew) {
            clientEntity.getData().setTenant(clientEntity.getData().getNamespaceId());
        }
        if (CommonUtils.isEmpty(clientEntity.getData().getAppName())) {
            clientEntity.getData().setAppName("");
        }
        if (CommonUtils.isEmpty(clientEntity.getData().getConfigTags())) {
            clientEntity.getData().setConfigTags("");
        }

        HttpEntity<MultiValueMap<String, Object>> httpEntity = formHttpEntity(clientEntity);
        String url = url(NacosAPI.CONFIGS, clientEntity) + "&" + "username=" + ContextUtil.getUser();
        log.info("nacos config save or update url: {}", url);
        return invoke(url, HttpMethod.POST, httpEntity, Boolean.class);
    }

    public Boolean delete(ClientEntity<ConfigDeleteVO> clientEntity) {
        boolean isBatch = CommonUtils.isNotEmpty(clientEntity.getData().getIds());

        String url = url(NacosAPI.CONFIGS, clientEntity) + "&" + "username=" + ContextUtil.getUser();

        String urlParams = "";
        if (isBatch) {
            urlParams += "&delType=ids&ids=" + clientEntity.getData().getIds();
        } else {
            urlParams += "&dataId=" + clientEntity.getData().getDataId() + "&group=" + clientEntity.getData().getGroup()
                + "&tenant=" + clientEntity.getData().getNamespaceId();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> formParameters = new LinkedMultiValueMap<>();
        formParameters.add("namespaceId", clientEntity.getData().getNamespaceId());

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(formParameters, headers);

        url += "&" + urlParams;
        log.info("nacos config delete url: {}", url);

        if (isBatch) {
            ResponseEntity<JSONObject> resp =
                restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, JSONObject.class);
            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                return resp.getBody().containsKey("data") && resp.getBody().getBoolean("data");
            }
            return false;
        }
        ResponseEntity<Boolean> resp = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, Boolean.class);

        return resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null ? resp.getBody() : false;
    }

    public ConfigDTO fetch(ClientEntity<ConfigQueryVO> clientEntity) {
        String url = urlWithData(NacosAPI.CONFIGS, clientEntity);
        log.info("nacos config fetch url: {}", url);
        return invoke(url, HttpMethod.GET, HttpEntity.EMPTY, ConfigDTO.class);
    }

    public JSONObject clone(ClientEntity<ConfigPolicyVO> clientEntity, ConfigCloneVO[] records) {

        String url = url(NacosAPI.CONFIGS, clientEntity);
        String urlParams = "clone=true&tenant=" + clientEntity.getData().getNamespaceId() + "&policy="
            + clientEntity.getData().getPolicy() + "&namespaceId=";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ConfigCloneVO[]> httpEntity = new HttpEntity<>(records, headers);
        url += "&" + urlParams;
        log.info("nacos config fetch url: {}", url);
        JSONObject resp = restTemplate.postForObject(url, httpEntity, JSONObject.class);
        AssertUtils.isNull(resp, PubError.CONNECT);
        if (resp.containsKey("code") && resp.getInteger("code") == 200) {
            return resp.getJSONObject("data");
        }
        return null;
    }

    /**
     * http://192.168.0.201:8848/nacos/v1/cs/configs? exportV2=true&tenant=&group=&appName=&ids=48,65 &username=nacos
     */
    public ResponseEntity<Resource> export(ClientEntity<ConfigExportVO> clientEntity) {

        String url = urlWithData(NacosAPI.CONFIGS, clientEntity);
        log.info("nacos configs export url: {}", url);
        return restTemplate.getForEntity(url, Resource.class);
    }

    public JSONObject importZip(ClientEntity<ConfigImportVO> clientEntity, MultipartFile file) throws Exception {
        String urlParams =
            "import=true&namespace=" + clientEntity.getData().getNamespaceId() + "&username=" + ContextUtil.getUser();
        String url = clientEntity.getUrl() + NacosAPI.CONFIGS + "?" + urlParams;
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();

        File f = new File(fsTmp, FileUtilsV2.generateDateUidFileName(file.getName()));
        FileUtil.writeFromStream(file.getInputStream(), f);
        FileSystemResource resource = new FileSystemResource(f);
        // 参数
        param.add("file", resource);
        param.add("policy", clientEntity.getData().getPolicy());

        log.info("nacos configs import url: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, headers);
        try {
            JSONObject resp = restTemplate.postForObject(url, httpEntity, JSONObject.class);
            AssertUtils.isNull(resp, PubError.CONNECT);

            log.debug("nacos configs import result: {}", resp);
            boolean success = resp.containsKey("code") && resp.getInteger("code") == 200;
            String msg = "";
            if (!success) {
                msg = resp.getString("message");
            }
            AssertUtils.isFalse(success, PubError.UNDEFINED, msg);
            return resp.getJSONObject("data");
        } finally {
            FileUtil.del(f);
        }
    }
}
