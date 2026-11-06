package org.hit.chiikaiwabe.constant;

public enum SortByDataConstant implements SortByInterface {

  USER {
    @Override
    public String getSortBy(String sortBy) {
      switch (sortBy) {
        case "firstName":
          return "firstName";
        case "lastName":
          return "lastName";
        case "lastModifiedDate":
          return "lastModifiedDate";
        default:
          return "createdDate";
      }
    }
  },

}
