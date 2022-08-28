package com.example.shadow.domain.shadow.service;


import com.example.shadow.domain.shadow.dto.FlowDto;
import com.example.shadow.domain.shadow.dto.KeywordDto;
import com.example.shadow.domain.shadow.entity.Flow;
import com.example.shadow.domain.shadow.entity.Flowchart;
import com.example.shadow.domain.shadow.entity.Keyword;
import com.example.shadow.domain.shadow.repository.FlowChartRepository;
import com.example.shadow.domain.shadow.repository.FlowRepository;
import com.example.shadow.domain.shadow.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlowChartService {

    private final FlowChartRepository flowChartRepository;
    private final KeywordRepository keywordRepository;
    private final FlowRepository flowRepository;


    public void create(List<KeywordDto> keywords) {

        for (KeywordDto k : keywords) {
            Keyword keyword = keywordRepository.findByName(k.getName());

            for(int i = 1 ; i <= k.getFlow().size(); i ++){

                FlowDto f = k.getFlow().get(i-1);

                Flow flow = flowRepository.findByNameAndDescription(f.getName(), f.getDescription());
                Flowchart flowchart = new Flowchart();
                flowchart.setKeyword(keyword);
                flowchart.setFlow(flow);
                flowchart.setSeq(i);

                flowChartRepository.save(flowchart);
            }

        }

    }
}
