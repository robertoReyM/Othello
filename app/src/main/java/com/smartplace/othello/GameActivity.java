package com.smartplace.othello;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.ArrayList;


public class GameActivity extends Activity {

    private LinearLayout[][] mLayoutSquares;
    private int[][] mSquares;
    private ArrayList<Square> mPossibilities;
    private int mCurrentType;
    private int mOpponentsType;
    private int mSquareCounter = 0;

    private static final int NO_PIECE = 0;
    public static final int WHITE_PIECE = 1;
    public static final int BLACK_PIECE = 2;
    private static final int MAX_HEIGHT = 8;
    private static final int MAX_WIDTH = 8;
    private static final int STRATEGY_THRESHOLD = 45;

    //game strategy
    private int[][] mStructureBoard = new int[][]{
           {  5, -5,  1, -1, -1,  1, -5,  5 },
           { -5, -5,  1, -1, -1,  1, -5, -5 },
           {  1,  1,  1, -1, -1,  1,  1,  1 },
           { -1, -1, -1,  0,  0, -1, -1, -1 },
           { -1, -1, -1,  0,  0, -1, -1, -1 },
           {  1,  1,  1, -1, -1,  1,  1,  1 },
           { -5, -5,  1, -1, -1,  1, -5, -5 },
           {  5, -5,  1, -1, -1,  1, -5,  5 }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        LinearLayout layoutBoard = (LinearLayout)findViewById(R.id.layout_board);
        mLayoutSquares = new LinearLayout[MAX_WIDTH][MAX_HEIGHT];
        mSquares = new int[MAX_WIDTH][MAX_HEIGHT];
        mPossibilities = new ArrayList<Square>();
        mCurrentType = getIntent().getIntExtra("type",BLACK_PIECE);


        LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT,1f);
        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f);

        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        //set rows
        for(int i = 0; i<MAX_HEIGHT;i++){

            LinearLayout rowLayout = new LinearLayout(getBaseContext());
            rowLayout.setLayoutParams(rowParams);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            //set columns
            for(int i2 = 0; i2<MAX_WIDTH;i2++){
                mLayoutSquares[i][i2] = new LinearLayout(getBaseContext());
                mLayoutSquares[i][i2].setLayoutParams(itemParams);
                mLayoutSquares[i][i2].setOrientation(LinearLayout.VERTICAL);
                mLayoutSquares[i][i2].setBackgroundResource(R.drawable.bg_square);
                mSquares[i][i2]=NO_PIECE;
                //set image
                ImageView imagePiece = new ImageView(getBaseContext());
                imagePiece.setLayoutParams(imageParams);
                int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
                imagePiece.setPadding(padding,padding,padding,padding);
                mLayoutSquares[i][i2].addView(imagePiece);
                rowLayout.addView(mLayoutSquares[i][i2]);
            }
            layoutBoard.addView(rowLayout);
        }
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        // Gets the layout params that will allow you to resize the layout
        ViewGroup.LayoutParams params = layoutBoard.getLayoutParams();
        // Changes the height and width to the specified *pixels*
        params.height = width;

        setInitialBoard();
        //if your type is white then black starts
        if(mCurrentType == WHITE_PIECE){
            mOpponentsType = BLACK_PIECE;
            setSquareHints(mOpponentsType);
        }else if (mCurrentType == BLACK_PIECE){
            mOpponentsType = WHITE_PIECE;
            setSquareHints(mCurrentType);
        }
    }

    public String getWinner(){

        String winner = "";
        int counterWhites = 0;
        int counterBlacks = 0;
        int winnerType = 0;
        //analyze white pieces
        for(int i = 0; i<MAX_HEIGHT;i++){
            //check columns
            for(int i2 = 0; i2<MAX_WIDTH;i2++){
                if(mSquares[i][i2]==WHITE_PIECE) {
                    counterWhites++;
                }else if (mSquares[i][i2]==BLACK_PIECE){
                    counterBlacks++;
                }
            }
        }
        if(counterBlacks<counterWhites){
            winnerType = WHITE_PIECE;
        }else{
            winnerType = BLACK_PIECE;
        }
        if(winnerType == mCurrentType){
            if(mCurrentType == BLACK_PIECE){
                winner = "YOU WIN: "+ counterBlacks+" to "+counterWhites;
            }else{
                winner = "YOU WIN: "+ counterWhites+" to "+counterBlacks;
            }
        }else{
            if(mCurrentType == BLACK_PIECE){
                winner = "YOU LOSE: "+ counterBlacks+" to "+counterWhites;
            }else{
                winner = "YOU LOSE: "+ counterWhites+" to "+counterBlacks;
            }
        }
        return winner;
    }
    public void setInitialBoard(){
        ((ImageView)mLayoutSquares[3][3].getChildAt(0)).setImageResource(R.drawable.white_piece);
        ((ImageView)mLayoutSquares[4][3].getChildAt(0)).setImageResource(R.drawable.black_piece);
        ((ImageView)mLayoutSquares[4][4].getChildAt(0)).setImageResource(R.drawable.white_piece);
        ((ImageView)mLayoutSquares[3][4].getChildAt(0)).setImageResource(R.drawable.black_piece);

        mSquares[3][3]=WHITE_PIECE;
        mSquares[4][3]=BLACK_PIECE;
        mSquares[4][4]=WHITE_PIECE;
        mSquares[3][4]=BLACK_PIECE;

        mSquareCounter = 4;


    }

    public void setSquareHints(int type){
        mPossibilities = getPossiblePositions(type);

        if(type == mCurrentType){
            if(mPossibilities.size() == 0){
                Toast.makeText(getBaseContext(),"END OF THE MATCH\n"+getWinner(),Toast.LENGTH_SHORT).show();
            }
            for(final Square square : mPossibilities) {
                mLayoutSquares[square.y][square.x].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        setSquare(square);
                    }
                });
            }
        }else{
            if(mPossibilities.size()!=0){
                setSquare(getBestPosition(mPossibilities));
            }else{
                Toast.makeText(getBaseContext(),"END OF THE MATCH\n"+getWinner(),Toast.LENGTH_SHORT).show();
            }

        }
    }
    public ArrayList<Square> getPossiblePositions(int type){
        ArrayList<Square> positions = new ArrayList<Square>();

        //go through enemies' pieces
        if(type == BLACK_PIECE){
            //analyze white pieces
            for(int i = 0; i<MAX_HEIGHT;i++){
                //check columns
                for(int i2 = 0; i2<MAX_WIDTH;i2++){
                    if(mSquares[i][i2]==WHITE_PIECE) {
                        Square square = new Square();
                        square.y = i;
                        square.x = i2;
                        square.type = WHITE_PIECE;
                        checkVerticalPossibilities(square, positions);
                        checkHorizontalPossibilities(square, positions);
                        checkDiagonalUpPossibilities(square, positions);
                        checkDiagonalDownPossibilities(square, positions);
                    }
                }
            }

        }else if(type == WHITE_PIECE){
            //analyze black pieces
            for(int i = 0; i<MAX_HEIGHT;i++){
                //check columns
                for(int i2 = 0; i2<MAX_WIDTH;i2++){
                    if(mSquares[i][i2]==BLACK_PIECE) {
                        Square square = new Square();
                        square.y = i;
                        square.x = i2;
                        square.type = BLACK_PIECE;
                        checkVerticalPossibilities(square, positions);
                        checkHorizontalPossibilities(square, positions);
                        checkDiagonalUpPossibilities(square, positions);
                        checkDiagonalDownPossibilities(square, positions);
                    }
                }
            }
        }

        for (Square square : positions){
            mLayoutSquares[square.y][square.x].setBackgroundResource(R.drawable.bg_square_hint);
        }
        return  positions;
    }
    public Square getBestPosition(ArrayList<Square> positions){
        int bestPositionIndex = 0;
        int bestPositionValue = -10;
        Square square;

        if(mSquareCounter<STRATEGY_THRESHOLD) {
            //use strategy board to define best move
            for (int i = 0; i < positions.size(); i++) {
                Square square1 = positions.get(i);
                int positionValue = mStructureBoard[square1.y][square1.x];
                if (bestPositionValue < positionValue) {
                    bestPositionValue = positionValue;
                    bestPositionIndex = i;
                }
            }
            square = positions.get(bestPositionIndex);
        }else{

            //check for material or conversion rate
            square = new Square();
            square.type = positions.get(0).type;
            int[][] materialBoard = new int[][]{
                { 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0 },
                { 0, 0, 0, 0, 0, 0, 0, 0 }
            };
            for(Square position:positions){
                materialBoard[position.y][position.x]+=position.conversionRate;
            }
            for(int i = 0; i<MAX_HEIGHT;i++){

                for(int i2 = 0; i2<MAX_WIDTH;i2++){
                    if(bestPositionValue<materialBoard[i][i2]){
                        bestPositionValue = materialBoard[i][i2];
                        square.y = i;
                        square.x = i2;
                        square.conversionRate = bestPositionValue;
                    }
                }
            }

        }
        return square;
    }
    public void setSquare(Square square1){
        final Square square = new Square();
        square.y = square1.y;
        square.x = square1.x;
        square.type = square1.type;
        //First remove hints
        for(Square hintSquare: mPossibilities){
            mLayoutSquares[hintSquare.y][hintSquare.x].setBackgroundResource(R.drawable.bg_square);
            mLayoutSquares[hintSquare.y][hintSquare.x].setOnClickListener(null);
        }
        mPossibilities.clear();
        mSquares[square.y][square.x] = square.type;
        mSquareCounter++;
        if(square.type == BLACK_PIECE){
            ((ImageView) mLayoutSquares[square.y][square.x].getChildAt(0)).setImageResource(R.drawable.black_piece);
        }else {
            ((ImageView) mLayoutSquares[square.y][square.x].getChildAt(0)).setImageResource(R.drawable.white_piece);
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                changeVerticalPieces(square);
                changeHorizontalPieces(square);
                changeDiagonalUpPieces(square);
                changeDiagonalDownPieces(square);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(square.type == mCurrentType){

                                setSquareHints(mOpponentsType);
                            }else{

                                setSquareHints(mCurrentType);
                            }
                        }
                    }, 500);

            }
        }, 1000);
    }

    private void changeHorizontalPieces(Square square){
        //check left for opponents pieces
        ArrayList<Square> piecesToChange = new ArrayList<Square>();
        boolean isAValidLine = false;
        if(square.x !=0){
            for(int i = square.x -1;i>=0;i--) {
                //go up checking for opponents pieces
                if(mSquares[square.y][i]!=square.type && mSquares[square.y][i]!=NO_PIECE){
                    Square square1 = new Square();
                    square1.y = square.y;
                    square1.x = i;
                    if (square.type == BLACK_PIECE) {
                        square1.type = WHITE_PIECE;
                    } else {
                        square1.type = BLACK_PIECE;
                    }
                    piecesToChange.add(square1);
                }else if(mSquares[square.y][i]==NO_PIECE){
                    i = -1;
                    piecesToChange.clear();
                }else if(mSquares[square.y][i]==square.type){
                    i = -1;
                    isAValidLine = true;
                }
            }
            if(isAValidLine){
                for(Square pieceToChange:piecesToChange){
                    mSquares[pieceToChange.y][pieceToChange.x] = square.type;
                    if(square.type == BLACK_PIECE){
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.black_piece);
                    }else {
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.white_piece);
                    }
                }
            }
        }

        piecesToChange.clear();
        isAValidLine = false;
        if(square.x !=(MAX_HEIGHT-1)){
            for(int i = square.x +1;i<MAX_HEIGHT;i++) {
                //go up checking for opponents pieces
                if(mSquares[square.y][i]!=square.type && mSquares[square.y][i]!=NO_PIECE){
                    Square square1 = new Square();
                    square1.y = square.y;
                    square1.x = i;
                    if (square.type == BLACK_PIECE) {
                        square1.type = WHITE_PIECE;
                    } else {
                        square1.type = BLACK_PIECE;
                    }
                    piecesToChange.add(square1);
                }else if(mSquares[square.y][i]==NO_PIECE){
                    i = MAX_HEIGHT;
                    piecesToChange.clear();
                }else if(mSquares[square.y][i]==square.type){
                    i = MAX_HEIGHT;
                    isAValidLine = true;
                }
            }
            if(isAValidLine){
                for(Square pieceToChange:piecesToChange){
                    mSquares[pieceToChange.y][pieceToChange.x] = square.type;
                    if(square.type == BLACK_PIECE){
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.black_piece);
                    }else {
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.white_piece);
                    }
                }
            }
        }
    }
    private void changeVerticalPieces(Square square){
        //check left for opponents pieces
        ArrayList<Square> piecesToChange = new ArrayList<Square>();
        boolean isAValidLine = false;
        if(square.y !=0){
            for(int i = square.y -1;i>=0;i--) {
                //go up checking for opponents pieces
                if(mSquares[i][square.x]!=square.type && mSquares[i][square.x]!=NO_PIECE){
                    Square square1 = new Square();
                    square1.y = i;
                    square1.x = square.x;
                    if (square.type == BLACK_PIECE) {
                        square1.type = WHITE_PIECE;
                    } else {
                        square1.type = BLACK_PIECE;
                    }
                    piecesToChange.add(square1);
                }else if(mSquares[i][square.x]==NO_PIECE){
                    i = -1;
                    piecesToChange.clear();
                }else if(mSquares[i][square.x]==square.type){
                    i = -1;
                    isAValidLine = true;
                }
            }
            if(isAValidLine){
                for(Square pieceToChange:piecesToChange){
                    mSquares[pieceToChange.y][pieceToChange.x] = square.type;
                    if(square.type == BLACK_PIECE){
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.black_piece);
                    }else {
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.white_piece);
                    }
                }
            }
        }

        isAValidLine = false;
        piecesToChange.clear();
        if(square.y !=(MAX_WIDTH-1)){
            for(int i = square.y +1;i<MAX_WIDTH;i++) {
                //go up checking for opponents pieces
                if(mSquares[i][square.x]!=square.type && mSquares[i][square.x]!=NO_PIECE){
                    Square square1 = new Square();
                    square1.y = i;
                    square1.x = square.x;
                    if (square.type == BLACK_PIECE) {
                        square1.type = WHITE_PIECE;
                    } else {
                        square1.type = BLACK_PIECE;
                    }
                    piecesToChange.add(square1);
                }else if(mSquares[i][square.x]==NO_PIECE){
                    i = MAX_WIDTH;
                    piecesToChange.clear();
                }else if(mSquares[i][square.x]==square.type){
                    i = MAX_WIDTH;
                    isAValidLine = true;
                }
            }
            if(isAValidLine){
                for(Square pieceToChange:piecesToChange){
                    mSquares[pieceToChange.y][pieceToChange.x] = square.type;
                    if(square.type == BLACK_PIECE){
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.black_piece);
                    }else {
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.white_piece);
                    }
                }
            }
        }
    }
    private void changeDiagonalDownPieces(Square square){
        //check left for opponents pieces
        ArrayList<Square> piecesToChange = new ArrayList<Square>();
        boolean isAValidLine = false;
        if(square.y !=(MAX_WIDTH-1) && square.x !=(MAX_WIDTH-1)){
            //check for an opposite type piece going right down
            for(int i = square.y+1;i< MAX_HEIGHT;i++){
                //find an opposite piece
                int nextWidth = square.x + i - square.y;
                if (nextWidth < MAX_WIDTH) {
                    if (mSquares[i][nextWidth] != square.type && mSquares[i][nextWidth] != NO_PIECE) {
                        Square square1 = new Square();
                        square1.y = i;
                        square1.x = nextWidth;
                        if (square.type == BLACK_PIECE) {
                            square1.type = WHITE_PIECE;
                        } else {
                            square1.type = BLACK_PIECE;
                        }
                        piecesToChange.add(square1);
                    } else if (mSquares[i][nextWidth] == NO_PIECE) {
                        //ig no piece get out of this routine
                        i = MAX_HEIGHT;
                    } else if(mSquares[i][nextWidth]==square.type){
                        i = MAX_HEIGHT;
                        isAValidLine = true;
                    }
                }else{
                    i=MAX_HEIGHT;
                }
            }

            if(isAValidLine){
                for(Square pieceToChange:piecesToChange){
                    mSquares[pieceToChange.y][pieceToChange.x] = square.type;
                    if(square.type == BLACK_PIECE){
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.black_piece);
                    }else {
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.white_piece);
                    }
                }
            }
        }

        isAValidLine = false;
        piecesToChange.clear();
        if(square.y !=0 && square.x !=0){
            for (int i = square.y-1; i>=0;i--) {
                int nextWidth = square.x + i - square.y;
                if(nextWidth>=0) {
                    //find an opposite piece
                    if (mSquares[i][nextWidth] != square.type && mSquares[i][nextWidth] != NO_PIECE) {
                        Square square1 = new Square();
                        square1.y = i;
                        square1.x = nextWidth;
                        if (square.type == BLACK_PIECE) {
                            square1.type = WHITE_PIECE;
                        } else {
                            square1.type = BLACK_PIECE;
                        }
                        piecesToChange.add(square1);
                    } else if (mSquares[i][nextWidth] == NO_PIECE) {
                        //check for no piece and get out of this routine
                        i = -1;
                    } else if(mSquares[i][nextWidth]==square.type){
                        i = -1;
                        isAValidLine = true;
                    }
                }else{
                    i=-1;
                }
            }

            if(isAValidLine){
                for(Square pieceToChange:piecesToChange){
                    mSquares[pieceToChange.y][pieceToChange.x] = square.type;
                    if(square.type == BLACK_PIECE){
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.black_piece);
                    }else {
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.white_piece);
                    }
                }
            }
        }
    }
    private void changeDiagonalUpPieces(Square square){
        //check left for opponents pieces
        ArrayList<Square> piecesToChange = new ArrayList<Square>();
        boolean isAValidLine = false;
        if(square.y !=(MAX_WIDTH-1) && square.x !=0){
            for (int i = square.y+1; i < MAX_HEIGHT; i++) {
                //find an opposite piece
                int nextWidth = square.x - i + square.y;
                if (nextWidth >=0) {
                    if (mSquares[i][nextWidth] != square.type && mSquares[i][nextWidth] != NO_PIECE) {
                        Square square1 = new Square();
                        square1.y = i;
                        square1.x = nextWidth;
                        if (square.type == BLACK_PIECE) {
                            square1.type = WHITE_PIECE;
                        } else {
                            square1.type = BLACK_PIECE;
                        }
                        piecesToChange.add(square1);
                    } else if (mSquares[i][nextWidth] == NO_PIECE) {
                        //ig no piece get out of this routine
                        i = MAX_HEIGHT;
                    } else  if(mSquares[i][nextWidth]==square.type){
                        i = MAX_HEIGHT;
                        isAValidLine = true;
                    }
                }else{
                    i=MAX_HEIGHT;
                }
            }


            if(isAValidLine){
                for(Square pieceToChange:piecesToChange){
                    mSquares[pieceToChange.y][pieceToChange.x] = square.type;
                    if(square.type == BLACK_PIECE){
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.black_piece);
                    }else {
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.white_piece);
                    }
                }
            }
        }

        isAValidLine = false;
        piecesToChange.clear();
        if(square.y !=0 && square.x!=(MAX_WIDTH-1)){
            for(int i = square.y-1;i>=0;i--){
                int nextWidth = square.x - i + square.y;
                if(nextWidth<MAX_HEIGHT) {
                    //find an opposite piece
                    if (mSquares[i][nextWidth] != square.type && mSquares[i][nextWidth] != NO_PIECE) {
                        Square square1 = new Square();
                        square1.y = i;
                        square1.x = nextWidth;
                        if (square.type == BLACK_PIECE) {
                            square1.type = WHITE_PIECE;
                        } else {
                            square1.type = BLACK_PIECE;
                        }
                        piecesToChange.add(square1);
                    } else if (mSquares[i][nextWidth] == NO_PIECE) {
                        //check for no piece and get out of this routine
                        i = -1;
                    } else if(mSquares[i][nextWidth]==square.type){
                        i = -1;
                        isAValidLine = true;
                    }
                }else{
                    i=-1;
                }
            }


            if(isAValidLine){
                for(Square pieceToChange:piecesToChange){
                    mSquares[pieceToChange.y][pieceToChange.x] = square.type;
                    if(square.type == BLACK_PIECE){
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.black_piece);
                    }else {
                        ((ImageView) mLayoutSquares[pieceToChange.y][pieceToChange.x].getChildAt(0)).setImageResource(R.drawable.white_piece);
                    }
                }
            }
        }
    }

    private void checkHorizontalPossibilities(Square square,ArrayList<Square> positions) {
        boolean isThereOppositeRight = false;
        boolean isThereOppositeLeft = false;
        int oppositeRightCtr = 1;
        int oppositeLeftCtr = 1;


        //check left for empty square
        if (square.x != 0) {
            if (mSquares[square.y][square.x - 1] == NO_PIECE) {
                //check for an opposite type piece going right
                for (int i = square.x+1; i < MAX_HEIGHT; i++) {
                    //find an opposite piece
                    if (mSquares[square.y][i] != square.type && mSquares[square.y][i] != NO_PIECE) {
                        isThereOppositeRight = true;
                    } else if (mSquares[square.y][i] == NO_PIECE) {
                        //check for no piece and get out of this routine
                        i = MAX_HEIGHT;
                    } else {
                        //this means its a piece of the same type
                        oppositeRightCtr++;
                    }
                }

                if (isThereOppositeRight) {
                    //check if its not the last square
                    Square square1 = new Square();
                    square1.y = square.y;
                    square1.x = square.x - 1;
                    square1.conversionRate = oppositeRightCtr;
                    if (square.type == BLACK_PIECE) {
                        square1.type = WHITE_PIECE;
                    } else {
                        square1.type = BLACK_PIECE;
                    }
                    positions.add(square1);

                }
            }
        }

        //check right for empty square
        if(square.x != (MAX_HEIGHT-1)){
            if(mSquares[square.y][square.x +1]==NO_PIECE){
                //check for an opposite type piece going left
                for(int i = square.x-1;i>=0;i--){
                    //find an opposite piece
                    if(mSquares[square.y][i]!=square.type && mSquares[square.y][i]!=NO_PIECE){
                        isThereOppositeLeft = true;
                    }else if(mSquares[square.y][i]==NO_PIECE){
                        //check for no piece and get out of this routine
                        i = -1;
                    }else{
                        //this means its a piece of the same type
                        oppositeLeftCtr++;
                    }
                }

                if(isThereOppositeLeft){
                    //check if its not the last square
                    Square square1 = new Square();
                    square1.y = square.y;
                    square1.x = square.x +1;
                    square1.conversionRate = oppositeLeftCtr;
                    if(square.type == BLACK_PIECE){
                        square1.type = WHITE_PIECE;
                    }else{
                        square1.type = BLACK_PIECE;
                    }
                    positions.add(square1);
                }
            }
        }

    }
    private void checkVerticalPossibilities(Square square,ArrayList<Square> positions){
        boolean isThereOppositeUp = false;
        boolean isThereOppositeDown = false;
        int oppositeUpCtr = 1;
        int oppositeDownCtr = 1;


        //check up for empty square
        if(square.y !=0) {
            if (mSquares[square.y - 1][square.x] == NO_PIECE) {
                //check for an opposite type piece going down
                for (int i = square.y+1; i < MAX_WIDTH; i++) {
                    //find an opposite piece
                    if (mSquares[i][square.x] != square.type && mSquares[i][square.x] != NO_PIECE) {
                        isThereOppositeDown = true;
                    } else if (mSquares[i][square.x] == NO_PIECE) {
                        //check for no piece and get out of this routine
                        i = MAX_WIDTH;
                    } else {
                        //this means its a piece of the same type
                        oppositeDownCtr++;
                    }
                }
                if (isThereOppositeDown) {
                    //check if its not the last square
                    if (square.y != 0) {
                        Square square1 = new Square();
                        square1.y = square.y - 1;
                        square1.x = square.x;
                        square1.conversionRate = oppositeDownCtr;
                        if (square.type == BLACK_PIECE) {
                            square1.type = WHITE_PIECE;
                        } else {
                            square1.type = BLACK_PIECE;
                        }
                        positions.add(square1);
                    }
                }
            }
        }
        //check down for empty square
        if(square.y !=(MAX_WIDTH-1)) {
            if (mSquares[square.y + 1][square.x] == NO_PIECE) {
                //check for an opposite type piece going up
                for (int i = square.y-1; i >= 0; i--) {
                    //find an opposite piece
                    if (mSquares[i][square.x] != square.type && mSquares[i][square.x] != NO_PIECE) {
                        isThereOppositeUp = true;
                    } else if (mSquares[i][square.x] == NO_PIECE) {
                        //check for no piece and get out of this routine
                        i = -1;
                    } else {
                        //this means its a piece of the same type
                        oppositeUpCtr++;
                    }
                }
                if (isThereOppositeUp) {
                    //check if its not the last square
                    Square square1 = new Square();
                    square1.y = square.y + 1;
                    square1.x = square.x;
                    square1.conversionRate = oppositeUpCtr;
                    if (square.type == BLACK_PIECE) {
                        square1.type = WHITE_PIECE;
                    } else {
                        square1.type = BLACK_PIECE;
                    }
                    positions.add(square1);

                }
            }
        }

    }
    private void checkDiagonalDownPossibilities(Square square,ArrayList<Square> positions){
        boolean isThereOppositeDiagonalRightDown = false;
        boolean isThereOppositeDiagonalLeftUp = false;
        int oppositeDiagonalRightDownCtr = 1;
        int oppositeDiagonalLeftUpCtr = 1;


        //check diagonal up left for empty square
        if(square.y !=0 && square.x !=0) {
            if (mSquares[square.y - 1][square.x - 1] == NO_PIECE) {
                //check for an opposite type piece going right down
                for(int i = square.y+1;i< MAX_HEIGHT;i++){
                    //find an opposite piece
                    int nextWidth = square.x + i - square.y;
                    if (nextWidth < MAX_WIDTH) {
                        if (mSquares[i][nextWidth] != square.type && mSquares[i][nextWidth] != NO_PIECE) {
                            isThereOppositeDiagonalRightDown = true;
                        } else if (mSquares[i][nextWidth] == NO_PIECE) {
                            //ig no piece get out of this routine
                            i = MAX_HEIGHT;
                        } else {
                            //this means its a piece of the same type
                            oppositeDiagonalRightDownCtr++;
                        }
                    }else{
                        i=MAX_HEIGHT;
                    }
                }
                //check for an empty square left down
                if (isThereOppositeDiagonalRightDown) {
                    Square square1 = new Square();
                    square1.y = square.y - 1;
                    square1.x = square.x - 1;
                    square1.conversionRate = oppositeDiagonalRightDownCtr;
                    if (square.type == BLACK_PIECE) {
                        square1.type = WHITE_PIECE;
                    } else {
                        square1.type = BLACK_PIECE;
                    }
                    positions.add(square1);

                }
            }
        }
        //check diagonal right down for empty square
        if(square.y != (MAX_WIDTH-1) && square.x != (MAX_HEIGHT-1)){
            if(mSquares[square.y +1][square.x +1]==NO_PIECE){
                //check for an opposite type piece going diagonal left up
                for (int i = square.y-1; i>=0;i--) {
                    int nextWidth = square.x + i - square.y;
                    if(nextWidth>=0) {
                        //find an opposite piece
                        if (mSquares[i][nextWidth] != square.type && mSquares[i][nextWidth] != NO_PIECE) {
                            isThereOppositeDiagonalLeftUp = true;
                        } else if (mSquares[i][nextWidth] == NO_PIECE) {
                            //check for no piece and get out of this routine
                            i = -1;
                        } else {
                            //this means its a piece of the same type
                            oppositeDiagonalLeftUpCtr++;
                        }
                    }else{
                        i=-1;
                    }
                }
                if(isThereOppositeDiagonalLeftUp){
                    //check if its not the last square
                    Square square1 = new Square();
                    square1.y = square.y +1;
                    square1.x = square.x +1;
                    square1.conversionRate = oppositeDiagonalLeftUpCtr;
                    if(square.type == BLACK_PIECE){
                        square1.type = WHITE_PIECE;
                    }else{
                        square1.type = BLACK_PIECE;
                    }
                    positions.add(square1);
                }
            }
        }

    }
    private void checkDiagonalUpPossibilities(Square square,ArrayList<Square> positions){
        boolean isThereOppositeDiagonalLeftDown = false;
        boolean isThereOppositeDiagonalRightUp = false;
        int oppositeDiagonalLeftDownCtr =1;
        int oppositeDiagonalRightUpCtr=1;


        //check diagonal right up for empty square
        if(square.y !=0 && square.x !=(MAX_HEIGHT-1)) {
            if (mSquares[square.y - 1][square.x + 1] == NO_PIECE) {
                //check for an opposite type piece going left down
                for (int i = square.y+1; i < MAX_HEIGHT; i++) {
                    //find an opposite piece
                    int nextWidth = square.x - i + square.y;
                    if (nextWidth >=0) {
                        if (mSquares[i][nextWidth] != square.type && mSquares[i][nextWidth] != NO_PIECE) {
                            isThereOppositeDiagonalLeftDown = true;
                        } else if (mSquares[i][nextWidth] == NO_PIECE) {
                            //ig no piece get out of this routine
                            i = MAX_HEIGHT;
                        } else {
                            //this means its a piece of the same type
                            oppositeDiagonalLeftDownCtr++;
                        }
                    }else{
                        i=MAX_HEIGHT;
                    }
                }
                if (isThereOppositeDiagonalLeftDown) {
                    Square square1 = new Square();
                    square1.y = square.y - 1;
                    square1.x = square.x + 1;
                    square1.conversionRate = oppositeDiagonalLeftDownCtr;
                    if (square.type == BLACK_PIECE) {
                        square1.type = WHITE_PIECE;
                    } else {
                        square1.type = BLACK_PIECE;
                    }
                    positions.add(square1);

                }
            }
        }
        //check diagonal left down for empty square
        if(square.y != (MAX_WIDTH-1) && square.x != 0){
            if(mSquares[square.y +1][square.x -1]==NO_PIECE){
                //check for an opposite type piece going diagonal right up
                for(int i = square.y-1;i>=0;i--){
                    int nextWidth = square.x - i + square.y;
                    if(nextWidth<MAX_HEIGHT) {
                        //find an opposite piece
                        if (mSquares[i][nextWidth] != square.type && mSquares[i][nextWidth] != NO_PIECE) {
                            isThereOppositeDiagonalRightUp = true;
                        } else if (mSquares[i][nextWidth] == NO_PIECE) {
                            //check for no piece and get out of this routine
                            i = -1;
                        } else {
                            //this means its a piece of the same type
                            oppositeDiagonalRightUpCtr++;
                        }
                    }else{
                        i=-1;
                    }
                }
                if(isThereOppositeDiagonalRightUp){
                    //check if its not the last square
                    Square square1 = new Square();
                    square1.y = square.y +1;
                    square1.x = square.x -1;
                    square1.conversionRate = oppositeDiagonalRightUpCtr;
                    if(square.type == BLACK_PIECE){
                        square1.type = WHITE_PIECE;
                    }else{
                        square1.type = BLACK_PIECE;
                    }
                    positions.add(square1);
                }
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
