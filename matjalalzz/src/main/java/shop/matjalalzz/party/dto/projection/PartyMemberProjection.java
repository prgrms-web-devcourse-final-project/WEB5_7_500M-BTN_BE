package shop.matjalalzz.party.dto.projection;

public interface PartyMemberProjection {

    Long getUserId();

    String getUserNickname();

    String getUserProfile();

    boolean getIsHost();
}
