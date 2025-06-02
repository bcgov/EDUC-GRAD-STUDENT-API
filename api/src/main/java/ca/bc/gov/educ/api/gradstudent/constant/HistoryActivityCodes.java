package ca.bc.gov.educ.api.gradstudent.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

public enum HistoryActivityCodes {

    USERDISTOT("USERDISTOT"),
    TRAXDELETE("TRAXDELETE"),
    USERDISTOC("USERDISTOC"),
    USERDISTRC("USERDISTRC"),
    GRADPROJECTED("GRADPROJECTED"),
    NONGRADYERUN("NONGRADYERUN"),
    USERDISTPSIP("USERDISTPSIP"),
    USEREDITTVRFLAG("USEREDITTVRFLAG"),
    USEREDITALGFLAG("USEREDITALGFLAG"),
    USERDISTPSIF("USERDISTPSIF"),
    USERSTUDARC("USERSTUDARC"),
    TVRDELETED("TVRDELETED"),
    USEREXAMADD("USEREXAMADD"),
    USEREXAMMOD("USEREXAMMOD"),
    USERCOURSEADD("USERCOURSEADD"),
    USERCOURSEMOD("USERCOURSEMOD"),
    USERCOURSEDEL("USERCOURSEDEL"),
    INSTITUTEALERT("INSTITUTEALERT"),
    PENALERT("PENALERT"),
    COREGALERT("COREGALERT"),
    CERTREGEN("CERTREGEN"),
    USERUNDOCMPL("USERUNDOCMPL"),
    MONTHLYDIST("MONTHLYDIST"),
    YEARENDDIST("YEARENDDIST"),
    USERDIST("USERDIST"),
    SUPPDIST("SUPPDIST"),
    NONGRADRUN("NONGRADRUN"),
    DATACONVERT("DATACONVERT"),
    GRADALG("GRADALG"),
    USERMERGE("USERMERGE"),
    USERDEMERGE("USERDEMERGE"),
    TRAXUPDATE("TRAXUPDATE"),
    TRAXADD("TRAXADD"),
    USEREDIT("USEREDIT"),
    USERADOPT("USERADOPT"),
    USERDELETE("USERDELETE");

    @Getter
    private final String code;

    HistoryActivityCodes(String code) {
        this.code = code;
    }

    private static final HistoryActivityCodes[] ENUM_VALUES = values();

    public static Optional<HistoryActivityCodes> findByCode(String code) {
        return Arrays.stream(ENUM_VALUES)
            .filter(e -> e.code.equals(code))
            .findFirst();
    }

}
