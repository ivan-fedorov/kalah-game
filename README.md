# MANCALA GAME 

Game can be ran in two ways: Console application and Web application

## Web application

For API documentation please use [swagger](http://localhost:8080/swagger-ui.html).

Each request except player creation requires special header `player-id` with players uuid.  

### How to start a game

* Create two players
* Create game(board)
* Make moves

## Console application

Console application was created to test main game logic. 
It has an ability to set up board size and number of stones in each pit. 

* Run main method in `ConsoleRunner` class
* Set up __board size__ and __number of stones__ in each pit
* Each player has it's own pit indexes, upper row is for Player2, bottom row for Player1
* Player can choose any pit index from 0 to `fields size - 1`
 
*NB:* order of Player2 pit indexes goes from right to left, whereas Player1 indexes goes from left to right 

---

    
|   |  6  |  5  |  4  |  3  |  2  |  1  |  0  |     |
|---|:---:|:---:|:---:|:---:|:---:|:---:|:---:|:---:|
| 0 |     |     |     |     |     |     |     |  0  |
|   |  0  |  1  |  2  |  3  |  4  |  5  |  6  |     |

