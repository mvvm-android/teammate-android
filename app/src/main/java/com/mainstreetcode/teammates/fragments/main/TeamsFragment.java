package com.mainstreetcode.teammates.fragments.main;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.adapters.TeamAdapter;
import com.mainstreetcode.teammates.adapters.viewholders.EmptyViewHolder;
import com.mainstreetcode.teammates.baseclasses.MainActivityFragment;
import com.mainstreetcode.teammates.model.Identifiable;
import com.mainstreetcode.teammates.model.Team;
import com.mainstreetcode.teammates.util.ScrollManager;

import java.util.List;

/**
 * Searches for teams
 */

public final class TeamsFragment extends MainActivityFragment
        implements
        View.OnClickListener,
        TeamAdapter.TeamAdapterListener {

    private static final int[] EXCLUDED_VIEWS = {R.id.team_list};

    private List<Identifiable> roles;

    public static TeamsFragment newInstance() {
        TeamsFragment fragment = new TeamsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getStableTag() {
        String superResult = super.getStableTag();
        Fragment target = getTargetFragment();
        if (target != null) superResult += ("-" + target.getTag() + "-" + getTargetRequestCode());
        return superResult;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roles = roleViewModel.getModelList(userViewModel.getCurrentUser().getId());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_teams, container, false);

        scrollManager = ScrollManager.withRecyclerView(rootView.findViewById(R.id.team_list))
                .withEmptyViewholder(new EmptyViewHolder(rootView, R.drawable.ic_group_black_24dp, R.string.no_team))
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(new TeamAdapter(roles, this))
                .withStaggeredGridLayoutManager(2)
                .build();

        View altToolbar = rootView.findViewById(R.id.alt_toolbar);
        altToolbar.setVisibility(isTeamPicker() ? View.VISIBLE : View.INVISIBLE);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setFabIcon(R.drawable.ic_search_white_24dp);
        setFabClickListener(this);
        if (!isTeamPicker()) setToolbarTitle(getString(R.string.my_teams));

        String userId = userViewModel.getCurrentUser().getId();
        disposables.add(roleViewModel.getMore(userId).subscribe(this::onTeamsUpdated, defaultErrorHandler));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_teams, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.action_search);
        if (item != null && isTeamPicker()) item.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                showFragment(TeamSearchFragment.newInstance());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public int[] staticViews() {return EXCLUDED_VIEWS;}

    @Override
    public boolean showsBottomNav() {return true;}

    @Override
    public boolean showsFab() {
        return !isTeamPicker();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onTeamClicked(Team team) {
        Fragment target = getTargetFragment();
        boolean canPick = target != null && target instanceof TeamAdapter.TeamAdapterListener;

        if (canPick) ((TeamAdapter.TeamAdapterListener) target).onTeamClicked(team);
        else showFragment(TeamDetailFragment.newInstance(team));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                toggleBottomSheet(false);
                showFragment(TeamSearchFragment.newInstance());
                break;
        }
    }

    private void onTeamsUpdated(DiffUtil.DiffResult result) {
        boolean isEmpty = roles.isEmpty();
        if (isTeamPicker()) toggleFab(isEmpty);
        scrollManager.onDiff(result);
    }

    private boolean isTeamPicker() {
        return getTargetRequestCode() != 0;
    }
}
