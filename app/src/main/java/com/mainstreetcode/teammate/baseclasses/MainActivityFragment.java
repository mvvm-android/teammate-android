package com.mainstreetcode.teammate.baseclasses;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mainstreetcode.teammate.activities.MainActivity;
import com.mainstreetcode.teammate.model.Message;
import com.mainstreetcode.teammate.model.Team;
import com.mainstreetcode.teammate.util.Logger;
import com.mainstreetcode.teammate.util.ScrollManager;
import com.mainstreetcode.teammate.viewmodel.ChatViewModel;
import com.mainstreetcode.teammate.viewmodel.EventViewModel;
import com.mainstreetcode.teammate.viewmodel.FeedViewModel;
import com.mainstreetcode.teammate.viewmodel.LocalRoleViewModel;
import com.mainstreetcode.teammate.viewmodel.LocationViewModel;
import com.mainstreetcode.teammate.viewmodel.MediaViewModel;
import com.mainstreetcode.teammate.viewmodel.RoleViewModel;
import com.mainstreetcode.teammate.viewmodel.TeamMemberViewModel;
import com.mainstreetcode.teammate.viewmodel.TeamViewModel;
import com.mainstreetcode.teammate.viewmodel.UserViewModel;

/**
 * Class for Fragments in {@link com.mainstreetcode.teammate.activities.MainActivity}
 */

public class MainActivityFragment extends TeammatesBaseFragment {

    protected ScrollManager scrollManager;
    protected FeedViewModel feedViewModel;
    protected RoleViewModel roleViewModel;
    protected UserViewModel userViewModel;
    protected TeamViewModel teamViewModel;
    protected EventViewModel eventViewModel;
    protected MediaViewModel mediaViewModel;
    protected ChatViewModel chatViewModel;
    protected LocationViewModel locationViewModel;
    protected LocalRoleViewModel localRoleViewModel;
    protected TeamMemberViewModel teamMemberViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        localRoleViewModel = ViewModelProviders.of(this).get(LocalRoleViewModel.class);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onAttach(Context context) {
        super.onAttach(context);
        ViewModelProvider provider = ViewModelProviders.of(getActivity());
        feedViewModel = provider.get(FeedViewModel.class);
        roleViewModel = provider.get(RoleViewModel.class);
        userViewModel = provider.get(UserViewModel.class);
        teamViewModel = provider.get(TeamViewModel.class);
        eventViewModel = provider.get(EventViewModel.class);
        mediaViewModel = provider.get(MediaViewModel.class);
        chatViewModel = provider.get(ChatViewModel.class);
        locationViewModel = provider.get(LocationViewModel.class);
        teamMemberViewModel = provider.get(TeamMemberViewModel.class);

        defaultErrorHandler.addAction(() -> {if (scrollManager != null) scrollManager.reset();});
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (scrollManager != null) scrollManager.clear();
    }

    @Override
    protected void handleErrorMessage(Message message) {
        if (message.isUnauthorizedUser()) signOut();
        else super.handleErrorMessage(message);

        boolean isIllegalTeamMember = message.isIllegalTeamMember();
        boolean shouldGoBack = isIllegalTeamMember || message.isInvalidObject();

        if (isIllegalTeamMember) teamViewModel.updateDefaultTeam(Team.empty());

        Activity activity = getActivity();
        if (activity == null) return;

        if (shouldGoBack) activity.onBackPressed();
    }

    protected void toggleBottomSheet(boolean show) {
        PersistentUiController controller = getPersistentUiController();
        if (controller instanceof BottomSheetController) {
            ((BottomSheetController) controller).toggleBottomSheet(show);
        }
    }

    protected void onInconsistencyDetected(IndexOutOfBoundsException exception) {
        Logger.log(getStableTag(), "Inconsistent Recyclerview", exception);
        Activity activity = getActivity();
        if (activity != null) activity.onBackPressed();
    }

    protected void signOut() {
        userViewModel.signOut().subscribe(
                success -> MainActivity.startRegistrationActivity(getActivity()),
                throwable -> MainActivity.startRegistrationActivity(getActivity())
        );
    }
}