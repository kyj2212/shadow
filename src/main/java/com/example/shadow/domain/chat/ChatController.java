package com.example.shadow.domain.chat;

import com.example.shadow.domain.member.service.MemberService;
import com.example.shadow.domain.shadow.entity.*;
import com.example.shadow.domain.shadow.service.FlowChartService;
import com.example.shadow.domain.shadow.service.KeywordService;
import com.example.shadow.domain.shadow.service.QuestionService;
import com.example.shadow.domain.shadow.service.ShadowService;
import com.example.shadow.global.result.ResultResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@Slf4j
public class ChatController {
    // ėŽėë
//    private static String secretKey = "SHVTeG5SemVXRk9KcU1oSU1VVWpWeW1MQmxCY0xzSk4=";
//    private static String apiUrl = "https://z16j1lin9x.apigw.ntruss.com/custom/v1/7654/bb5bef27a0dd572b921c6b22c71e79115c1d4cca1dcbd766d269fa6c2d5bd9ad";
    private static String secretKey = "U1VoTXZua1BOT3hqVFNjS0Rqemxrc0JCdEZIc2RrSmg=";
    private static String apiUrl = "https://f4by9xj6rc.apigw.ntruss.com/custom/v1/7840/5324714f7fb3a860659ad721c75c0d677d2e1f165bc0f300db74ddf7a6e2e8b1";
    private final QuestionService questionService;
    private final ShadowService shadowService;
    private final MemberService memberService;
    private final KeywordService keywordService;
    private final FlowChartService flowChartService;
    private final Long testShadowId = 1L;

    //ëģīëž ëĐėļė§ëĨž ëĪėīëēėė ė ęģĩíīėĪ ėíļíëĄ ëģęē―íīėĢžë ëĐėë
    public static String makeSignature(String message, String secretKey) {

        String encodeBase64String = "";

        try {
            byte[] secrete_key_bytes = secretKey.getBytes("UTF-8");

            SecretKeySpec signingKey = new SecretKeySpec(secrete_key_bytes, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
            encodeBase64String = Base64.encodeBase64String(rawHmac);

            return encodeBase64String;

        } catch (Exception e) {
            System.out.println(e);
        }

        return encodeBase64String;
    }

    //ëģīëž ëĐėļė§ëĨž ëĪėīëē ėąëīė íŽë§·ėžëĄ ëģęē―íīėĢžë ëĐėë
    public static String getReqMessage(String chatMessage) {

        String requestBody = "";

        try {

            JSONObject obj = new JSONObject();

            long timestamp = new Date().getTime();

            System.out.println("##" + timestamp); // timestamp ėķë Ĩ

            obj.put("version", "v2");
            obj.put("userId", "U47b00b58c90f8e47428af8b7bddcda3d");
            obj.put("timestamp", timestamp);

            JSONObject bubbles_obj = new JSONObject();

            bubbles_obj.put("type", "text");

            JSONObject data_obj = new JSONObject();
            chatMessage = chatMessage.replace("\"", "");
            data_obj.put("description", chatMessage);

            bubbles_obj.put("type", "text");
            bubbles_obj.put("data", data_obj);

            JSONArray bubbles_array = new JSONArray();
            bubbles_array.add(bubbles_obj);

            obj.put("bubbles", bubbles_array);
            obj.put("event", "send");

            requestBody = obj.toString();
        } catch (Exception e) {
            System.out.println("## Exception : " + e);
        }

        return requestBody;
    }

    @GetMapping("/chat/{apiKey}")
    public String chatGET(Model model, @PathVariable String apiKey) {

        log.info("apikeyëĄ ëģęē―ë ę°ė ė ę°ė ļėĪëę°? = {} ", apiKey);

        // ėēė ę°ė ļėŽ ë, íīëđ ėŽėĐė chatė ėë favoriteė ę°ė ļėĪė. (íėí ė ëģī : member idė shadow id ę° íėíëĪ.)
        // ė°ė  ę°ė íė, member idę° 1ėīęģ  shadow idę° 1ėļ ęēė ėėíëĪęģ  íė.

        HashMap<String, String> favoriteKeywords = new HashMap<>();

        //long memberId = 1;

        long shadowId = shadowService.findByApiKey(apiKey).getId();

        //Member member = memberService.findById(1);
        Shadow shadow = shadowService.findById(shadowId);

        //íīëđ shadowė ėë keyword ëŠĐëĄė ę°ė ļėĪė. (favoriteėī ėēīíŽëėīėë)
        List<Keyword> keywords = keywordService.findByShadowAndFavorite(shadow);

        //ę·ļëŽëĐī ėīė  keywordė ë§ë ėĩėĒ ëŠĐė ė§ urlė ę°ė ļėĪė§
        //ėĶ flowė ë§ė§ë§ ëĻęģė urlė ė°ūėėĪėë ėëŊļę° ëëĪ.
        keywords.forEach(keyword -> {

            //1. flowchartėė íīëđ keywordę° ėë ęē ėĪėė, order descėėžëĄ, limit 1ė ęąļėīė ę°ė ļėĪė
            Flowchart flowchart = flowChartService.findByKeyword(keyword);

            //2. ę°ė ļėĻ flowchartė ė ėĨëėīėë flow idëĨž íĩíīė, flowėė urlė ę°ė ļėĪė
            log.info("keywordėė ę°ė ļėĻ flowchartė seqę° ë§ė§ë§ėļ, flow id = {}", flowchart.getFlow().getId());

            //3. keywordė urlė íĐėģė MapėžëĄ íīė modelė ëĢėīėĢžė
            favoriteKeywords.put(keyword.getName(), flowchart.getFlow().getUrl());
        });

        model.addAttribute("favoriteKeywords", favoriteKeywords);

        return "chatbot/chat";
    }
    @PostMapping("/chat/{apiKey}")
    public String chatGET(Model model, @RequestParam("shadow_keyword") String keywordName,
                          @RequestParam("shadow_seq") Integer seq, @RequestParam("shadow_url") String url, @PathVariable String apiKey) {


        log.info("ėëëĶŽėĪėėë ? = {} ", apiKey);

        HashMap<String, String> favoriteKeywords = new HashMap<>();

        long shadowId = shadowService.findByApiKey(apiKey).getId();
        Shadow shadow = shadowService.findById(shadowId);

        List<Keyword> keywords = keywordService.findByShadowAndFavorite(shadow);
        keywords.forEach(keyword -> {
            Flowchart flowchart = flowChartService.findByKeyword(keyword);
            log.info("keywordėė ę°ė ļėĻ flowchartė seqę° ë§ė§ë§ėļ, flow id = {}", flowchart.getFlow().getId());
            favoriteKeywords.put(keyword.getName(), flowchart.getFlow().getUrl());
        });

        model.addAttribute("favoriteKeywords", favoriteKeywords);
        Keyword keyword = keywordService.findByNameAndShadow(keywordName,shadow);

        String targetUrl = getFlowcharts(keyword).get(seq-1).getFlow().getUrl();
        String description = seq == getFlowcharts(keyword).size() ? null : getFlowcharts(keyword).get(seq).getFlow().getDescription();
        log.debug("[scenario] targetUrl : "+targetUrl);
        log.debug("[scenario] description : "+description);
        if(url.equals(targetUrl)){
            seq++;
        }else{
            log.debug("[scenario] targetUrl : "+targetUrl+" url : "+url+" ëĪëĶ");
        }

        log.debug("[scenario] flowcharts : "+getFlowcharts(keyword));
        log.debug("[scenario] seq : "+seq);
        model.addAttribute("keyword",keywordName);
        model.addAttribute("flowcharts",getFlowcharts(keyword));
        model.addAttribute("seq",seq);
        model.addAttribute("description",description);
        model.addAttribute("fixedMsg",getFixedMsg(keyword));

        return "chatbot/chat";
    }

    @RequestMapping("/chat/question/{apiKey}")
    @SendTo("/topic/shadow")
    @ResponseBody
    public ResponseEntity<ResultResponse> sendScenario(String question, @PathVariable String apiKey) throws IOException {

        // íėĪíļėĐ shadow , testShadowIdëĄ ė§ė 
        long shadowId = shadowService.findByApiKey(apiKey).getId();
        Shadow shadow = shadowService.findById(shadowId);
        log.debug("[scenario] shadow : " + shadow.getId() + " , " + shadow.getName() + ", " + shadow.getMainurl());
        String reqMessage = question;
        reqMessage = reqMessage.replace("\"", "");


        // [scenario] ėė
        Question q = questionService.existByQuestion(reqMessage) ? questionService.findByQuestionAndShadow(reqMessage,shadow) : null;
        if(q!=null){

                log.debug("[scenario][case1] íĪėë DBėė ëėķ ėė");
                Keyword keyword = getKeyword(reqMessage,shadow);
                log.debug("[scenario] keyword : " + keyword);

                shadow.addDbCall();
                shadowService.save(shadow);

                return getMessage(keyword);

        } else {
            log.debug("[scenario][case2] íĪėë APIėė ëėķ ėė");
            URL url = new URL(apiUrl);

            String message = getReqMessage(question);
            String encodeBase64String = makeSignature(message, secretKey);

            //apiėëē ė ė (ėëē -> ėëē íĩė )
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json;UTF-8");
            con.setRequestProperty("X-NCP-CHATBOT_SIGNATURE", encodeBase64String);
            System.out.println("API íļėķ ėëĢ");

            // post request
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(message.getBytes("UTF-8"));
            wr.flush();
            wr.close();

            int responseCode = con.getResponseCode();

            if (responseCode == 200) { // ė ė íļėķ
                log.debug("[scenario] API ė ė íļėķ");

                shadow.addApiCall();
                shadowService.save(shadow);

                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
                String decodedString;
                String jsonString = "";
                while ((decodedString = in.readLine()) != null) {
                    jsonString = decodedString;
                }

                //ë°ėėĻ ę°ė ėļííë ëķëķ
                JSONParser jsonparser = new JSONParser();
                try {
                    JSONObject json = (JSONObject) jsonparser.parse(jsonString);
                    JSONArray bubblesArray = (JSONArray) json.get("bubbles");
                    JSONObject bubbles = (JSONObject) bubblesArray.get(0);
                    JSONObject data = (JSONObject) bubbles.get("data");
                    String description = (String) data.get("description");
                    log.debug("[scenario][case2] íĪėë APIėė ëėķ , description : " + description);
                    Keyword keyword = keywordService.findByNameAndShadow(description, shadow);
                    log.debug("[scenario][case2] íĪėë APIėė ëėķ , keyword : " + keyword);

                    if (keyword == null) {
                        String msg = "shadowę° ėīíīíė§ ëŠŧíėīė. ëĪė íëē ėë Ĩíī ėĢžėļė.";
                        log.error("[scenario] ėëŽ ëĐėļė§ : " + msg);
                        in.close();
                        return ResponseEntity.ok(ResultResponse.of("NOT_FOUND_KEYWORD", msg, false));
                    }

                    if (!questionService.existByQuestionAndKeyword(reqMessage,keyword)) { // DBė ė ėĨėī ėëėī ėė ęē―ė° DBė keyword ė ėĨ
                        log.debug("[scenario] dbė ė ėĨ : "+reqMessage);
                        create(reqMessage, keyword);
                    }
                    in.close();



                    return getMessage(keyword);

                } catch (Exception e) {
                    String msg = "shadowę° ėīíīíė§ ëŠŧíėīė. ëĪė íëē ėë Ĩíī ėĢžėļė.";
                    log.error("[scenario] ėëŽ ëĐėļė§ : " + msg);
                    e.printStackTrace();
                    in.close();
                    return ResponseEntity.ok(ResultResponse.of("NOT_FOUND_KEYWORD", msg, false));
                }

            } else {  // ėëŽ ë°ė
                log.debug("[scenario] API ė ė íļėķ ėĪíĻ");
                question = con.getResponseMessage();
                log.error("[scenario] ėëŽ ëĐėļė§ : " + question);
            }

            log.error("[scenario] ėëŽ ëĐėļė§ : " + question);
            return ResponseEntity.ok(ResultResponse.of("ERROR_GET_KEYWORD", question, false));
        }
    }

    public void create(String question, Keyword keyword) {
        questionService.create(question, keyword);
    }

    private Shadow getShadow(String url) {
        return shadowService.findByMainurl(url);
    }

    private Keyword getKeyword(String reqMessage,Shadow shadow) {
        return questionService.findByQuestionAndShadow(reqMessage,shadow).getKeyword();
    }

    private List<Flowchart> getFlowcharts(Keyword keyword) {
        List<Flowchart> flowcharts = keyword.getFlowcharts();
        if (flowcharts.size() == 0) {
            log.error("[scenario][error] keyword has not flowcharts");
        }
        return flowcharts;
    }

    private List<Flow> getFlows(List<Flowchart> flowcharts) {
        List<Flow> flows = flowcharts.stream().map(Flowchart::getFlow).collect(Collectors.toList());
        if (flows.size() == 0) {
            log.error("[scenario][error] flowcharts has not flows");
        }
        log.debug("[scenario] flows : " + flows);
        return flows;
    }

    private String getFixedMsg(Keyword keyword){
        // ëėëķ ėëī ëĐėļė§
        return """
                shadowę° \'%s\' ė ėëīíĐëëĪ. <br>
                ėë ëđĻę° ëēížė ë°ëž ëëŽėĢžėļė. <br>
                """.formatted(keyword.getName());
    }
    private ResponseEntity<ResultResponse> getMessage(Keyword keyword) {

        List<Flowchart> flowcharts = getFlowcharts(keyword);

        // ė ėēī flow ėëĩ
        return ResponseEntity.ok(ResultResponse.of("GET_FLOWS_FROM_KEYWORD", getFixedMsg(keyword), flowcharts));
    }

}