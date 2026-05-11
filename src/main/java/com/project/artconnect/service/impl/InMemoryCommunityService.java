package com.project.artconnect.service.impl;

import com.project.artconnect.model.CommunityMember;
import com.project.artconnect.model.Exhibition;
import com.project.artconnect.model.Review;
import com.project.artconnect.service.CommunityService;
import java.util.*;

public class InMemoryCommunityService implements CommunityService {
    private final Map<String, CommunityMember> members = new LinkedHashMap<>();

    public InMemoryCommunityService() {}

    public void initData() {
        addMember("Alice Wonderland", "alice@art.com",       "Paris");
        addMember("Bob Ross",         "bob@happytrees.com",  "London");
        addMember("Charlie Brown",    "charlie@peanuts.com", "New York");
    }

    private CommunityMember addMember(String name, String email, String city) {
        CommunityMember m = new CommunityMember(name, email);
        m.setCity(city); m.setMembershipType("Premium");
        members.put(name, m); return m;
    }

    @Override public List<CommunityMember> getAllMembers()                        { return new ArrayList<>(members.values()); }
    @Override public Optional<CommunityMember> getMemberByName(String name)       { return Optional.ofNullable(members.get(name)); }
    @Override public List<Review> getReviewsByMember(CommunityMember member)      { return member == null ? Collections.emptyList() : member.getReviews(); }
    @Override public void createMember(CommunityMember m)                         { members.put(m.getName(), m); }
    @Override public void updateMember(CommunityMember m)                         { members.put(m.getName(), m); }
    @Override public void deleteMember(String name)                               { members.remove(name); }
}
