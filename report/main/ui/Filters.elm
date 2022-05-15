module Filters exposing (..)


type alias Filters =
    List Filter


type Filter
    = ComponentNameEquals String


empty =
    []


filterBy name filters =
    [ ComponentNameEquals name ]
