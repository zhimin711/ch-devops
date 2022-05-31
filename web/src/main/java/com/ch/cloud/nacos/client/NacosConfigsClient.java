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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
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

    @Autowired
    private RestTemplate restTemplate;
    @Value("${fs.path.tmp:/tmp}")
    private String       fsTmp;

    public Boolean add(ClientEntity<ConfigVO> entity) {
        return save(entity, true);
    }

    public Boolean edit(ClientEntity<ConfigVO> entity) {
        return save(entity, false);
    }

    private Boolean save(ClientEntity<ConfigVO> entity, boolean isNew) {
        if (isNew) {
            entity.getData().setTenant(entity.getData().getNamespaceId());
        }
        if (CommonUtils.isEmpty(entity.getData().getAppName())) {
            entity.getData().setAppName("");
        }
        if (CommonUtils.isEmpty(entity.getData().getConfigTags())) {
            entity.getData().setConfigTags("");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(formParameters(entity), headers);
        String url = entity.getUrl() + NacosAPI.CONFIGS + "?" + "username=" + ContextUtil.getUser();
        log.info("nacos config save or update url: {}", url);
        return restTemplate.postForObject(url, httpEntity, Boolean.class);
    }

    public InvokerPage.Page<ConfigDTO> fetchPage(ClientEntity<ConfigsPageVO> entity) {
        Map<String, String> param = BeanUtilsV2.objectToMap(entity.getData());
        String urlParams = HttpUtil.toParams(param);
        String url = entity.getUrl() + NacosAPI.CONFIGS + "?" + urlParams;
        log.info("nacos configs page url: {}", url);
        JSONObject resp = restTemplate.getForObject(url, JSONObject.class);
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

    public Boolean delete(ClientEntity<ConfigDeleteVO> entity) {
        boolean isBatch = CommonUtils.isNotEmpty(entity.getData().getIds());
        String urlParams = "username=" + ContextUtil.getUser();
        if (isBatch) {
            urlParams += "&delType=ids&ids=" + entity.getData().getIds();
        } else {
            urlParams += "&dataId=" + entity.getData().getDataId()
                    + "&group=" + entity.getData().getGroup()
                    + "&tenant=" + entity.getData().getNamespaceId();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, Object> formParameters = new LinkedMultiValueMap<>();
        formParameters.add("namespaceId", entity.getData().getNamespaceId());

        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(formParameters, headers);

        String url = entity.getUrl() + NacosAPI.CONFIGS + "?" + urlParams;

        if (isBatch) {
            ResponseEntity<JSONObject> resp = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, JSONObject.class);
            if (resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null) {
                return resp.getBody().containsKey("data") && resp.getBody().getBoolean("data");
            }
            return false;
        }
        ResponseEntity<Boolean> resp = restTemplate.exchange(url, HttpMethod.DELETE, httpEntity, Boolean.class);

        return resp.getStatusCode() == HttpStatus.OK && resp.getBody() != null ? resp.getBody() : false;
    }

    public ConfigDTO fetch(ClientEntity<ConfigQueryVO> clientEntity) {
        Map<String, String> param = BeanUtilsV2.objectToMap(clientEntity.getData());
        String urlParams = HttpUtil.toParams(param);
        JSONObject resp = restTemplate.getForObject(clientEntity.getUrl() + NacosAPI.CONFIGS + "?" + urlParams, JSONObject.class);
        if (resp != null) return resp.toJavaObject(ConfigDTO.class);
        return null;
    }

    public JSONObject clone(ClientEntity<ConfigPolicyVO> clientEntity, ConfigCloneVO[] records) {
        String url = clientEntity.getUrl() + NacosAPI.CONFIGS + "?clone=true&tenant="
                + clientEntity.getData().getNamespaceId()
                + "&policy=" + clientEntity.getData().getPolicy()
                + "&namespaceId=";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ConfigCloneVO[]> httpEntity = new HttpEntity<>(records, headers);

        JSONObject resp = restTemplate.postForObject(url, httpEntity, JSONObject.class);
        AssertUtils.isNull(resp, PubError.CONNECT);
        if (resp.containsKey("code") && resp.getInteger("code") == 200) {
            return resp.getJSONObject("data");
        }
        return null;
    }

    /**
     * http://192.168.0.201:8848/nacos/v1/cs/configs?
     * exportV2=true&tenant=&group=&appName=&ids=48,65
     * &username=nacos
     */
    public ResponseEntity<Resource> export(ClientEntity<ConfigExportVO> clientEntity) {
        Map<String, String> param = BeanUtilsV2.objectToMap(clientEntity.getData());
        String urlParams = HttpUtil.toParams(param);
        return restTemplate.getForEntity(clientEntity.getUrl() + NacosAPI.CONFIGS + "?" + urlParams, Resource.class);
    }

    public JSONObject importZip(ClientEntity<ConfigImportVO> clientEntity, MultipartFile file) throws Exception {
        String urlParams = "import=true&namespace=" + clientEntity.getData().getNamespaceId() + "&username=" + ContextUtil.getUser();
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();

        File f = new File(fsTmp, FileUtilsV2.generateDateUidFileName(file.getName()));
        FileUtil.writeFromStream(file.getInputStream(), f);
        FileSystemResource resource = new FileSystemResource(f);
        //参数
        param.add("file", resource);
        param.add("policy", clientEntity.getData().getPolicy());


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        HttpEntity<MultiValueMap<String, Object>> httpEntity = new HttpEntity<>(param, headers);
        try {
            JSONObject resp = restTemplate.postForObject(clientEntity.getUrl() + NacosAPI.CONFIGS + "?" + urlParams, httpEntity, JSONObject.class);
            AssertUtils.isNull(resp, PubError.CONNECT);
            if (resp.containsKey("code") && resp.getInteger("code") == 200) {
                return resp.getJSONObject("data");
            }
        } finally {
            FileUtil.del(f);
        }
        return null;
    }
}
