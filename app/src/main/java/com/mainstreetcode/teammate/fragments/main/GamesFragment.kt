/*
 * MIT License
 *
 * Copyright (c) 2019 Adetunji Dahunsi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.mainstreetcode.teammate.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.DiffUtil
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.GameAdapter
import com.mainstreetcode.teammate.adapters.viewholders.EmptyViewHolder
import com.mainstreetcode.teammate.baseclasses.MainActivityFragment
import com.mainstreetcode.teammate.fragments.headless.TeamPickerFragment
import com.mainstreetcode.teammate.model.Competitive
import com.mainstreetcode.teammate.model.Event
import com.mainstreetcode.teammate.model.Game
import com.mainstreetcode.teammate.model.ListState
import com.mainstreetcode.teammate.model.Team
import com.mainstreetcode.teammate.model.User
import com.mainstreetcode.teammate.util.ScrollManager
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment
import com.tunjid.androidbootstrap.recyclerview.InteractiveViewHolder
import com.tunjid.androidbootstrap.recyclerview.diff.Differentiable

/**
 * Lists [tournaments][Event]
 */

class GamesFragment : MainActivityFragment(), GameAdapter.AdapterListener {

    private lateinit var team: Team
    private lateinit var items: List<Differentiable>

    override val fabStringResource: Int @StringRes get() = R.string.game_add

    override val fabIconResource: Int @DrawableRes get() = R.drawable.ic_add_white_24dp

    override val toolbarMenu: Int get() = R.menu.fragment_tournaments

    override val toolbarTitle: CharSequence get() = getString(R.string.games)

    override fun getStableTag(): String {
        val superResult = super.getStableTag()
        val tempTeam = arguments!!.getParcelable<Team>(ARG_TEAM)

        return if (tempTeam != null) superResult + "-" + tempTeam.hashCode()
        else superResult
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        team = arguments!!.getParcelable(ARG_TEAM)!!
        items = gameViewModel.getModelList(team)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_list_with_refresh, container, false)

        val refreshAction = Runnable { disposables.add(gameViewModel.refresh(team).subscribe(this::onGamesUpdated, defaultErrorHandler::invoke)) }

        scrollManager = ScrollManager.with<InteractiveViewHolder<*>>(rootView.findViewById(R.id.list_layout))
                .withPlaceholder(EmptyViewHolder(rootView, R.drawable.ic_score_white_24dp, R.string.no_games))
                .withRefreshLayout(rootView.findViewById(R.id.refresh_layout), refreshAction)
                .withEndlessScroll { fetchGames(false) }
                .addScrollListener { _, dy -> updateFabForScrollState(dy) }
                .addScrollListener { _, _ -> updateTopSpacerElevation() }
                .withInconsistencyHandler(this::onInconsistencyDetected)
                .withAdapter(GameAdapter(items, this))
                .withLinearLayoutManager()
                .build()

        return rootView
    }

    override fun onResume() {
        super.onResume()
        fetchGames(true)
        watchForRoleChanges(team, this::togglePersistentUi)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.action_pick_team -> TeamPickerFragment.change(requireActivity(), R.id.request_game_team_pick).let { true }
        else -> super.onOptionsItemSelected(item)
    }

    override val showsFab: Boolean
        get() {
            val sport = team.sport
            val supportsTournaments = sport.supportsCompetitions()
            return if (sport.betweenUsers()) supportsTournaments else supportsTournaments && localRoleViewModel.hasPrivilegedRole()
        }

    override fun onGameClicked(game: Game) {
        showFragment(GameFragment.newInstance(game))
    }

    override fun onClick(view: View) = when (view.id) {
        R.id.fab -> {
            val game = Game.empty(team)
            val entity: Competitive =
                    if (User.COMPETITOR_TYPE == game.refPath) userViewModel.currentUser
                    else teamViewModel.defaultTeam

            game.home.updateEntity(entity)
            showFragment(GameEditFragment.newInstance(game)).let { Unit }
        }
        else -> Unit
    }

    override fun provideFragmentTransaction(fragmentTo: BaseFragment): FragmentTransaction? = when {
        fragmentTo.stableTag.contains(TournamentEditFragment::class.java.simpleName) ->
            fragmentTo.listDetailTransition(TournamentEditFragment.ARG_TOURNAMENT)

        else -> super.provideFragmentTransaction(fragmentTo)
    }

    private fun fetchGames(fetchLatest: Boolean) {
        if (fetchLatest) scrollManager.setRefreshing()
        else toggleProgress(true)

        disposables.add(gameViewModel.getMany(team, fetchLatest).subscribe(this::onGamesUpdated, defaultErrorHandler::invoke))
    }

    private fun onGamesUpdated(result: DiffUtil.DiffResult) {
        toggleProgress(false)
        val supportsTournaments = team.sport.supportsCompetitions()
        scrollManager.onDiff(result)
        scrollManager.updateForEmptyList(ListState(R.drawable.ic_score_white_24dp,
                if (supportsTournaments) R.string.no_games
                else R.string.no_game_support))
    }

    companion object {

        private const val ARG_TEAM = "team"

        fun newInstance(team: Team): GamesFragment = GamesFragment().apply { arguments = bundleOf(ARG_TEAM to team) }
    }
}