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

package com.mainstreetcode.teammate.adapters.viewholders

import android.view.View
import androidx.core.view.ViewCompat.setTransitionName
import com.mainstreetcode.teammate.R
import com.mainstreetcode.teammate.adapters.UserHostListener
import com.mainstreetcode.teammate.model.JoinRequest
import com.mainstreetcode.teammate.util.getTransitionName

class JoinRequestViewHolder(
        itemView: View,
        delegate: UserHostListener
) : ModelCardViewHolder<JoinRequest>(itemView) {

    init {
        itemView.setOnClickListener { delegate.onJoinRequestClicked(model) }
    }

    override fun bind(model: JoinRequest) {
        super.bind(model)

        val item = model.user
        val context = itemView.context

        title.text = item.firstName
        subtitle.text = if (model.isTeamApproved && !model.isUserApproved)
            context.getString(R.string.user_invited, model.position.name)
        else
            context.getString(R.string.user_requests_join, model.position.name)

        setTransitionName(itemView, model.getTransitionName(R.id.fragment_header_background))
        setTransitionName(thumbnail, model.getTransitionName(R.id.fragment_header_thumbnail))
    }

}
