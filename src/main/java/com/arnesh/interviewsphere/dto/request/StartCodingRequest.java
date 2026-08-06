package com.arnesh.interviewsphere.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.arnesh.interviewsphere.constant.Difficulty;
import com.arnesh.interviewsphere.constant.Company;
import com.arnesh.interviewsphere.constant.Tag;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StartCodingRequest {
    private List<Company> company;
    private List<Tag> tag;
    private List<Difficulty> difficulty;
}
