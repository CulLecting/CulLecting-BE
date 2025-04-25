package com.hambugi.cullecting.domain.curtural.repository;

import com.hambugi.cullecting.domain.curtural.entity.CulturalEvent;
import com.hambugi.cullecting.domain.curtural.util.CodeNameEnum;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Map;

public class CulturalEventSpecifications {

    private static final Map<String, List<String>> THEME_CODE_MAP = Map.of(
            "어린이/청소년", List.of("어린이/청소년문화행사"),
            "가족", List.of("가족문화행사"),
            "어르신", List.of("어르신문화행사"),
            "여성", List.of("여성문화행사"),
            "기타", List.of("기타")
    );

    public static Specification<CulturalEvent> withCodename(String codename) {
        return (root, query, cb) -> {
            if (codename == null || codename.equals("전체")) return null;

            List<String> values = CodeNameEnum.getByLabel(codename)
                    .map(CodeNameEnum::getData)
                    .orElse(null);

            if (values == null || values.isEmpty()) {
                return cb.disjunction(); // 아무것도 없으면 결과 없음
            }

            return root.get("codename").in(values);
        };
    }

    public static Specification<CulturalEvent> withGuname(String guname) {
        return (root, query, cb) ->
                (guname == null || guname.equals("전체")) ? null : cb.equal(root.get("guname"), guname);
    }

    public static Specification<CulturalEvent> withIsFree(Boolean isFree) {
        return (root, query, cb) ->
                (isFree == null) ? null : cb.equal(root.get("isFree"), isFree);
    }

    public static Specification<CulturalEvent> withThemeCode(String themeCode) {
        return (root, query, cb) -> {
            if (themeCode == null || themeCode.isBlank() || themeCode.equals("전체")) return null;

            List<String> mapped = THEME_CODE_MAP.get(themeCode);
            if (mapped == null || mapped.isEmpty()) {
                return cb.disjunction(); // 잘못된 입력이면 결과 없음
            }

            return root.get("themeCode").in(mapped);
        };
    }
}
