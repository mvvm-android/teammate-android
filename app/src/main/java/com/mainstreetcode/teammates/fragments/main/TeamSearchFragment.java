package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.transition.AutoTransition;
import android.support.transition.TransitionManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamAdapter;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Team;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * Searches for teams
 */

public final class TeamSearchFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        SearchView.OnQueryTextListener,
        TeamAdapter.TeamAdapterListener {

    private static final int[] EXCLUDED_VIEWS = {R.id.team_list};

    private View createTeam;
    private RecyclerView recyclerView;
    private final List<Identifiable> teams = new ArrayList<>();

    private final Consumer<List<Team>> teamConsumer = (teams) -> {
        ViewGroup parent = (ViewGroup) createTeam.getParent();
        TransitionManager.beginDelayedTransition(parent, new AutoTransition());
        createTeam.setVisibility(View.VISIBLE);

        this.teams.clear();
        this.teams.addAll(teams);
        recyclerView.getAdapter().notifyDataSetChanged();
    };

    public static TeamSearchFragment newInstance() {
        TeamSearchFragment fragment = new TeamSearchFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_team_search, container, false);
        createTeam = rootView.findViewById(R.id.create_team);
        recyclerView = rootView.findViewById(R.id.team_list);

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setItemAnimator(null);
        recyclerView.setAdapter(new TeamAdapter(teams, this));

        createTeam.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_team_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchItem.expandActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setToolbarTitle(getString(R.string.team_search));

        disposables.add(teamViewModel.findTeams("").subscribe(teamConsumer, defaultErrorHandler));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        createTeam = null;
        recyclerView = null;
    }

    @Override
    public int[] staticViews() {return EXCLUDED_VIEWS;}

    @Override
    public boolean showsFab() {
        return false;
    }

    @Override
    public void onTeamClicked(Team team) {
        showFragment(TeamEditFragment.newJoinInstance(team));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.create_team:
                showFragment(TeamEditFragment.newCreateInstance());
                break;
        }
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String queryText) {
        if (getView() == null || TextUtils.isEmpty(queryText)) return true;
        disposables.add(teamViewModel.findTeams(queryText).subscribe(teamConsumer, defaultErrorHandler));
        return true;
    }
}
