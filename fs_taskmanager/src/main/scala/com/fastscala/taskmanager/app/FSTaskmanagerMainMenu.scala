package com.fastscala.taskmanager.app

import com.fastscala.demo.docs.navigation.DefaultBSMenuRenderer._
import com.fastscala.demo.docs.navigation.{BSMenu, RoutingMenuItem}

object FSTaskmanagerMainMenu extends BSMenu(
//  MenuSection("Task Manager")(
    new RoutingMenuItem("tasks")("Tasks", () => new TasksPage()),
    new RoutingMenuItem("users")("Users", () => new UsersPage()),
//  )
)
