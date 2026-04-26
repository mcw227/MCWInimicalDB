import java.util.List;

public class Pager<T> {

    public List<? extends T> list;
    
    private int pageSize;

    public int currentPage;

    private int current_start_index;

    private boolean atEnd = false;

    public Pager(List<? extends T> existingList, int pageSize) {
        this.list = existingList;
        this.pageSize = pageSize;
        this.currentPage = 0;
        this.current_start_index = 0;
        this.atEnd = existingList.isEmpty();
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /** Prints the current page */
    public void printCurrentPage() {
        if (list.size() == 0) {
            System.out.println("--- EMPTY LIST ---");
            return;
        }

        if (currentPage == list.size()/pageSize + 1) {
            nextPage();
            printCurrentPage();
            return;
        }

        System.out.printf("--- PAGE %d OF %d ---\n", currentPage+1, (int)Math.ceil((double)list.size()/pageSize));
        int end_index = ((current_start_index + pageSize) < list.size()) ? (current_start_index + pageSize) : list.size();
        //System.out.println(end_index); //debug
        
        for (int i = current_start_index; i < end_index; i++) {
            System.out.println(list.get(i));
        }
    }

    /** Increments page counter. Loops back to first page*/
    public void nextPage() {
        if (list.size() == 0) {
            current_start_index = 0;
            return;
        }

        if (current_start_index < list.size()) {
            int temp = current_start_index;
            current_start_index = ((current_start_index + pageSize) < list.size()) ? (current_start_index + pageSize) : 0;

            if (current_start_index == 0) {
                currentPage = 0;
            } else {
                currentPage++;
            }
        }

        else {
            current_start_index = 0;
            currentPage = 0;
        }
    }

    /** Decrements page counter */
    public void previousPage() {
        if (list.size() == 0) {
            currentPage = 0;
            current_start_index = 0;
            return;
        }

        if (current_start_index < list.size()) {
            int temp = current_start_index;

            if (current_start_index == 0) {
                if (pageSize >= list.size())
                    current_start_index = 0;
                else
                    current_start_index = (list.size() - list.size()%pageSize -1);
            }
            else {
                current_start_index = ((current_start_index - pageSize) < 0) ? (current_start_index - pageSize) : 0;
            }
            
            if (temp < current_start_index) //looped around
                currentPage = (list.size()/pageSize);
            else if (current_start_index == 0)
                currentPage = 0;
            else
                currentPage--;
        }

        else {
            current_start_index = 0;
            currentPage = 0;
        }
    }

    //Sets it to the page or to the max page if it is too long
    public void setPage(int pageNum) {
        currentPage = (pageNum <= (list.size() / pageSize)) ? pageNum : ((list.size() / pageSize)-1);
        current_start_index = pageSize * currentPage;
    }

    /** Prints current page and then moves to next page */
    public void printAndAdvance() {
        printCurrentPage();
        nextPage();
    }

    /**
     * @return true if list is empty, false if not
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }
}