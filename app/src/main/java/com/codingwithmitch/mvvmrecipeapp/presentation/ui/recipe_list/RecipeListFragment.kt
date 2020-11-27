package com.codingwithmitch.mvvmrecipeapp.presentation.ui.recipe_list

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.ScrollableController
import androidx.compose.foundation.gestures.rememberScrollableController
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.ExperimentalFocus
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.gesture.ScrollCallback
import androidx.compose.ui.gesture.scrollGestureFilter
import androidx.compose.ui.gesture.scrollorientationlocking.Orientation
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.codingwithmitch.mvvmrecipeapp.R
import com.codingwithmitch.mvvmrecipeapp.domain.model.Recipe
import com.codingwithmitch.mvvmrecipeapp.presentation.BaseApplication
import com.codingwithmitch.mvvmrecipeapp.presentation.components.FoodCategoryChip
import com.codingwithmitch.mvvmrecipeapp.presentation.components.RecipeCard
import com.codingwithmitch.mvvmrecipeapp.presentation.components.RecipeListLoading
import com.codingwithmitch.mvvmrecipeapp.presentation.ui.recipe_list.RecipeListEvent.*
import com.codingwithmitch.mvvmrecipeapp.util.TAG
import com.codingwithmitch.openchat.common.framework.presentation.theme.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@ExperimentalFocus
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RecipeListFragment: Fragment() {

    @Inject
    lateinit var application: BaseApplication

    private val viewModel: RecipeListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.compose_view, container, false
        ).apply {
            findViewById<ComposeView>(R.id.compose_view).setContent {

                val displayProgressBar by viewModel.loading.collectAsState()

                val query by viewModel.query.collectAsState()

                val selectedCategory by viewModel.selectedCategory.collectAsState()

                val categories = getAllFoodCategories()

                val recipes by viewModel.recipes.collectAsState()

                AppTheme(
                    darkTheme = !application.isLight,
                    progressBarIsDisplayed = displayProgressBar
                ){
                    Column(
                            modifier = Modifier
                                    .background(color = if(application.isLight) Grey1 else Black5)
                    ) {
                        SearchAppBar(
                            query = query,
                            onQueryChanged = viewModel::onQueryChanged,
                            onExecuteSearch = {
                                viewModel.onTriggerEvent(SearchEvent())
                            },
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onSelectedCategoryChanged = viewModel::onSelectedCategoryChanged,
                            categoryScrollPosition = viewModel._categoryScrollPosition,
                            onChangeCategoryScrollPosition = viewModel::onChangeCategoryScrollPosition,
                            onToggleTheme = application::toggleLightTheme,
                        )

                        if(!displayProgressBar){
                            RecipeList(recipes = recipes)
                        }
                        else{
                            RecipeListLoading()
                        }
                    }

                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }
}

@ExperimentalFocus
@Composable
fun SearchAppBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    onExecuteSearch: () -> Unit,
    categories: List<FoodCategory>,
    selectedCategory: FoodCategory?,
    onSelectedCategoryChanged: (String) -> Unit,
    categoryScrollPosition: Float,
    onChangeCategoryScrollPosition: (Float) -> Unit,
    onToggleTheme: () -> Unit,
){
    Surface(
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colors.surface,
            elevation = 8.dp,
            content = {
                Column {
                    Row(
                            modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                                modifier = Modifier
                                        .fillMaxWidth(.9f)
                                        .padding(8.dp),
                                value = query,
                                onValueChange = { onQueryChanged(it) },
                                label = {
                                    Text(text = "Search")
                                },
                                keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        imeAction = ImeAction.Done,
                                ),
                                leadingIcon = { Icon(Icons.Filled.Search) },
                                onImeActionPerformed = { action, softKeyboardController ->
                                    if (action == ImeAction.Next || action == ImeAction.Done) {
                                        softKeyboardController?.hideSoftwareKeyboard()
                                    }
                                    onExecuteSearch()
                                },
                                textStyle = TextStyle(color = MaterialTheme.colors.onSurface),
                                backgroundColor = MaterialTheme.colors.surface
                        )
                        ConstraintLayout(
                                modifier = Modifier.align(Alignment.CenterVertically)
                        ) {
                            val (menu) = createRefs()
                            IconButton(
                                    modifier = Modifier
                                            .constrainAs(menu) {
                                                end.linkTo(parent.end)
                                                linkTo(top = parent.top, bottom = parent.bottom)
                                            },
                                    onClick = onToggleTheme,
                                    icon = { Icon(Icons.Filled.MoreVert) },
                            )
                        }

                    }
                    val scrollState = rememberScrollState()
                    ScrollableRow(
                            modifier = Modifier.padding(start = 8.dp, bottom = 8.dp),
                            scrollState = scrollState,
                    ) {
                        // restore scroll position after rotation
                        scrollState.scrollTo(categoryScrollPosition)
                        // display FoodChips
                        for (category in categories) {
                            FoodCategoryChip(
                                    category = category.value,
                                    isSelected = selectedCategory == category,
                                    onSelectedCategoryChanged = {
                                        onChangeCategoryScrollPosition(scrollState.value)
                                        onSelectedCategoryChanged(it)
                                    },
                                    onExecuteSearch = onExecuteSearch,
                            )
                        }
                    }
                }
            }
    )
}




@ExperimentalCoroutinesApi
@Composable
fun RecipeList(
    recipes: List<Recipe>,
){

    // actual composable state
    val offset = remember { mutableStateOf(0f) }

    val interactionState = InteractionState()
    Log.d(TAG, "RecipeList: interaction state: ${interactionState}")
    interactionState.addInteraction(Interaction.Dragged)
    val scrollableController = rememberScrollableController(
            consumeScrollDelta = { delta ->
                Log.d(TAG, "RecipeList: scrolling: ${delta}")
                offset.value = offset.value + delta
                delta
            },
            interactionState = interactionState
    )
//    val state = rememberLazyListState(
////            interactionState = interactionState
//    )
    val state = rememberScrollState(
            interactionState = interactionState
    )
    Log.d(TAG, "RecipeList: state: ${state}")
    LazyColumnFor(
            items = recipes,
            modifier = Modifier
                    .scrollable(
                            orientation = Orientation.Vertical,
                            controller = scrollableController,
                            canScroll = { true },
                            enabled = true,
                            reverseDirection = true,
                            onScrollStarted = {
                                Log.d(TAG, "RecipeList: started.")
                            },
                            onScrollStopped = {
                                Log.d(TAG, "RecipeList: stopped")
                            },

                    ),
    ) { recipe ->
        RecipeCard(recipe = recipe)
    }
//    ScrollableColumn(
//            modifier = Modifier
//                    .scrollable(
//                            orientation = Orientation.Vertical,
//                            controller = scrollableController,
//                            canScroll = {true},
//                            enabled = true,
//                    )
//    ) {
//        for(recipe in recipes){
//            RecipeCard(recipe = recipe)
//        }
//    }

}



















