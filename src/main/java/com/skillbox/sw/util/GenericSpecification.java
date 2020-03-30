package com.skillbox.sw.util;

import java.time.LocalDate;
import java.util.List;
import javax.persistence.criteria.Path;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class GenericSpecification<T> {

  public Specification<T> byField(
      String fieldName,
      String operation,
      Object filter
  ) {
    return (r, q, b) -> {
      Path p = r.get(fieldName);
      if (operation.equals("in")) {
        return p.in((List) filter);
      } else if (operation.equalsIgnoreCase("%")) {
        if (p.getJavaType() == String.class) {
          return b.like(p, "%" + filter.toString() + "%");
        } else {
          return b.equal(p, filter);
        }
      } else if (operation.equals(">")) {
        if (p.getJavaType() == LocalDate.class) {
          return b.greaterThanOrEqualTo(p, (LocalDate) filter);
        }
        return b.greaterThanOrEqualTo(p, (LocalDate) filter);
      } else if (operation.equals("<")) {
        if (p.getJavaType() == LocalDate.class) {
          return b.lessThanOrEqualTo(p, (LocalDate) filter);
        }
        return b.lessThanOrEqualTo(p, filter.toString());
      }
      return null;
    };
  }

  public Specification<T> byFieldParam(
      String fieldName,
      String operation,
      String fieldParam,
      Object filter
  ) {
    return (r, q, b) -> {
      Path p = r.get(fieldName).get(fieldParam);
      if (operation.equals("in")) {
        return p.in((List) filter);
      } else if (operation.equals("%")) {
        if (p.getJavaType() == String.class) {
          return b.like(p, "%" + filter.toString() + "%");
        } else {
          return b.equal(p, filter);
        }
      } else if (operation.equals(">")) {
        if (p.getJavaType() == LocalDate.class) {
          return b.greaterThanOrEqualTo(p, (LocalDate) filter);
        }
        return b.greaterThanOrEqualTo(p, (LocalDate) filter);
      } else if (operation.equals("<")) {
        if (p.getJavaType() == LocalDate.class) {
          return b.lessThanOrEqualTo(p, (LocalDate) filter);
        }
        return b.lessThanOrEqualTo(p, filter.toString());
      }
      return null;
    };
  }
}