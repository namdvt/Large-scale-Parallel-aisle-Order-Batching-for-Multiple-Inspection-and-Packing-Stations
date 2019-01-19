import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nguyen on 7/9/2017.
 */
public class RandomGenerator {
    public int num_order = 12*80;
    public int num_item = 2;
    public int num_pickface = 60;
    public int num_aisle = 6;
    public int aisle_size = 10;
    public int str_mode = 0;
    public int cor_mode = 0;
    public int dry_run = 5000;
    public int [][] orders = new int[num_order + dry_run][num_pickface];
    public int [] order_size = new int[num_order];
    public int [][]order_aisle = new int[num_order][num_aisle];
    public double a_ratio=0.5, b_ratio=0.30, c_ratio=0.20, cost_cor=0.70;

    public RandomData getRandomValues() {
        List<Order> orderList = new ArrayList<Order>();
        int numOrder;
        int numAisle;
        int [][]Vrda;
        int []capa;
        double r_loc;
        int i, f, k, a, o, loc_pickface, rand_num, cur_height, cur_zone, sel_zone, cur_sector, sector_size, num_sector, sel_sector,
                cur_aisle, sel_height, sel_aisle, rel_pickface, cur_depth, zone_size, sel_depth, unit_height;
        for (o = 0; o < num_order + dry_run; o++) {
            if (o >= dry_run) {
                for (f = 0; f < num_pickface; f++)
                    orders[o - dry_run][f] = 0;
                for (a = 0; a < num_aisle; a++)
                    order_aisle[o - dry_run][a] = 0;
                order_size[o - dry_run] = 0;
            }

            if (str_mode < 4 || str_mode == 11) {

                r_loc = Math.random();// 0,...,0.99999999

                if (num_item % 2 == 0)
                    rand_num = (int) (num_item / 2 + (num_item + 1) * r_loc);
                else {
                    rand_num = (int) ((num_item + 1) / 2 + (num_item) * r_loc);
                }
                if (num_item == 100) {
                    rand_num = 1;
                    rand_num += (int) (1.0 / (2.0 * (1.0 - r_loc * 0.95)));
//				printf("%f  %d \n",r_loc, rand_num);
                }
                if (num_item > 1000) {
                    rand_num = num_item - 1000;

                }
            } else {
                //num_item = 2;
                rand_num = num_item;
                if (str_mode == 6) {
                    switch (o) {
                        case 0:
                            rand_num = 8;
                            break;
                        case 1:
                            rand_num = 4;
                            break;
                        case 2:
                            rand_num = 4;
                            break;
                        case 3:
                            rand_num = 4;
                            break;

                    }

                }
            }


            if (o >= dry_run)
                order_size[o - dry_run] = rand_num;
            for (i = 0; i < rand_num; i++) {

                r_loc = Math.random();
                //-----
                loc_pickface = (int) (r_loc * num_pickface);

                cur_aisle = loc_pickface / aisle_size;
                cur_depth = loc_pickface % aisle_size;
                //-----
                if (str_mode == 0) {
                    //-----
                    loc_pickface = (int) (r_loc * num_pickface);

                    cur_aisle = loc_pickface / aisle_size;
                    cur_depth = loc_pickface % aisle_size;
                    //-----

                    // the first item or random cor
                    if (i == 0 || cor_mode == 0) {
                        // the first item or random cor
                        loc_pickface = (int) (r_loc * num_pickface);

                        cur_aisle = loc_pickface / aisle_size;
                        cur_depth = loc_pickface % aisle_size;
                    } else {
                        rel_pickface = (int) (r_loc / cost_cor * aisle_size);
                        sel_aisle = cur_aisle;
                        sel_depth = rel_pickface;
                        if (cor_mode == 1)   // in-an-aisle
                        {
                            if (r_loc < cost_cor) {
                                rel_pickface = (int) (r_loc / cost_cor * aisle_size);

                                sel_aisle = cur_aisle;
                                sel_depth = rel_pickface;

                            } else {
                                rel_pickface = (int) ((r_loc - cost_cor) / (1.0 - cost_cor) * aisle_size * (num_aisle - 1));

                                sel_aisle = (int) rel_pickface / aisle_size;
                                sel_depth = (int) rel_pickface % aisle_size;

                                if (sel_aisle >= cur_aisle)
                                    sel_aisle++;
                            }

                        } else if (cor_mode == 2)   // in-a-zone
                        {
                            if (r_loc < cost_cor + (1.0 - cost_cor) / 3.0) {
                                rel_pickface = (int) (r_loc / (cost_cor + (1.0 - cost_cor) / 3.0) * 2 * aisle_size);

                                if (rel_pickface < aisle_size) {
                                    sel_aisle = 2 * (cur_aisle / 2);
                                    sel_depth = rel_pickface;
                                } else {
                                    sel_aisle = 2 * (cur_aisle / 2) + 1;
                                    sel_depth = rel_pickface - aisle_size;
                                }

                            } else {
                                rel_pickface = (int) ((r_loc - cost_cor - (1.0 - cost_cor) / 3.0) / (1.0 - cost_cor - (1.0 - cost_cor) / 3.0) * aisle_size * (num_aisle - 2));


                                if (rel_pickface < aisle_size) {
                                    sel_aisle = 2 * (cur_aisle / 2);
                                    sel_depth = rel_pickface;
                                } else {
                                    sel_aisle = 2 * (cur_aisle / 2) + 1;
                                    sel_depth = rel_pickface - aisle_size;
                                }

                                if (cur_aisle < 2)
                                    sel_aisle += 2;
                                else
                                    sel_aisle -= 2;
                            }

                        } else if (cor_mode == 3)   // diagonal one
                        {
                            if (r_loc < cost_cor) {
                                rel_pickface = (int) (r_loc / cost_cor * aisle_size);

                                sel_aisle = cur_aisle;
                                sel_depth = rel_pickface;

                                if ((cur_depth < aisle_size / 2 && sel_depth >= aisle_size / 2) ||
                                        (cur_depth >= aisle_size / 2 && sel_depth < aisle_size / 2)) {
                                    if (sel_depth < aisle_size / 2)
                                        sel_depth += (aisle_size / 2);
                                    else
                                        sel_depth -= (aisle_size / 2);

                                    if (sel_aisle % 2 == 0)
                                        sel_aisle += 1;
                                    else
                                        sel_aisle -= 1;
                                }

                            } else {
                                rel_pickface = (int) ((r_loc - cost_cor) / (1.0 - cost_cor) * aisle_size * (num_aisle - 1));

                                sel_aisle = (int) rel_pickface / aisle_size;
                                sel_depth = (int) rel_pickface % aisle_size;

                                if (sel_aisle >= cur_aisle)
                                    sel_aisle++;

                                if ((cur_aisle / 2 == sel_aisle / 2 && cur_depth < aisle_size / 2 && sel_depth < aisle_size / 2) ||
                                        (cur_aisle / 2 == sel_aisle / 2 && cur_depth >= aisle_size / 2 && sel_depth >= aisle_size / 2)) {
                                    if (sel_depth < aisle_size / 2)
                                        sel_depth += (aisle_size / 2);
                                    else
                                        sel_depth -= (aisle_size / 2);
                                    sel_aisle = cur_aisle;
                                }
                            }

                        } else if (cor_mode == 4)   // from the entrance
                        {
                            if (r_loc < cost_cor) {
                                unit_height = aisle_size / num_aisle;
                                rel_pickface = (int) ((r_loc / cost_cor) * aisle_size);

                                sel_aisle = (int) (rel_pickface / unit_height) % num_aisle;
                                sel_depth = unit_height * (int) (cur_depth / unit_height) + rel_pickface % unit_height;

                            } else {
                                unit_height = aisle_size / num_aisle;
                                rel_pickface = (int) (((r_loc - cost_cor) / (1.0 - cost_cor) * (num_aisle - 1) * aisle_size));

                                sel_aisle = (int) (rel_pickface / unit_height) % num_aisle;
                                sel_depth = unit_height * (int) (rel_pickface / unit_height / num_aisle) + rel_pickface % unit_height;

                                if (sel_depth >= cur_depth)
                                    sel_depth = sel_depth + unit_height;

                            }

                        } else if (cor_mode == 5)   // from the front
                        {
                            if (cur_aisle % 2 == 0)
                                cur_height = cur_depth;
                            else
                                cur_height = aisle_size - cur_depth - 1;

                            if (r_loc < cost_cor) {
                                unit_height = aisle_size / num_aisle;
                                rel_pickface = (int) ((r_loc / cost_cor) * aisle_size);

                                sel_aisle = (int) (rel_pickface / unit_height) % num_aisle;
                                sel_height = unit_height * (int) (cur_height / unit_height) + rel_pickface % unit_height;

                                if (sel_aisle % 2 == 1)
                                    sel_depth = aisle_size - sel_height - 1;
                                else
                                    sel_depth = sel_height;
                            } else {
                                unit_height = aisle_size / num_aisle;
                                rel_pickface = (int) (((r_loc - cost_cor) / (1.0 - cost_cor) * (num_aisle - 1) * aisle_size));

                                sel_aisle = (int) (rel_pickface / unit_height) % num_aisle;
                                sel_height = unit_height * (int) (rel_pickface / unit_height / num_aisle) + rel_pickface % unit_height;

                                if (sel_height >= cur_height)
                                    sel_height += unit_height;

                                if (sel_aisle % 2 == 1)
                                    sel_depth = aisle_size - sel_height - 1;
                                else
                                    sel_depth = sel_height;

                            }
                        }
                        loc_pickface = (int) (sel_aisle * aisle_size + sel_depth);

                    }

                } else if (str_mode == 1 || str_mode == 11) {


                    // 2 aisles
                    if (num_aisle == 2) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 1.0 / 2.0;
                        } else
                            r_loc = 1.0 / 2.0 + (r_loc - a_ratio) / (1.0 - a_ratio) * 1.0 / 2.0;
                    }
                    // 4 aisles
                    if (num_aisle == 4) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 1.0 / 4.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 1.0 / 4.0 + (r_loc - a_ratio) / b_ratio * 1.0 / 4.0;
                        } else
                            r_loc = 1.0 / 2.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 1.0 / 2.0;
                    }
                    // 6 aisles
                    if (num_aisle == 6) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 1.0 / 6.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 1.0 / 6.0 + (r_loc - a_ratio) / b_ratio * 2.0 / 6.0;
                        } else
                            r_loc = 1.0 / 2.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 3.0 / 6.0;
                    }

                    // 8 aisles
                    if (num_aisle == 8) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 2.0 / 8.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 2.0 / 8.0 + (r_loc - a_ratio) / b_ratio * 2.0 / 8.0;
                        } else
                            r_loc = 4.0 / 8.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 4.0 / 8.0;
                    }

                    // 10 aisles
                    if (num_aisle == 10) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 2.0 / 10.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 2.0 / 10.0 + (r_loc - a_ratio) / b_ratio * 2.0 / 10.0;
                        } else
                            r_loc = 4.0 / 10.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 6.0 / 10.0;
                    }

                    // 12 aisles
                    if (num_aisle == 12) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 2.0 / 12.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 2.0 / 12.0 + (r_loc - a_ratio) / b_ratio * 4.0 / 12.0;
                        } else
                            r_loc = 6.0 / 12.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 6.0 / 12.0;
                    }

                    // 14 aisles
                    if (num_aisle == 14) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 3.0 / 14.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 3.0 / 14.0 + (r_loc - a_ratio) / b_ratio * 3.0 / 14.0;
                        } else
                            r_loc = 6.0 / 14.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 8.0 / 14.0;
                    }

                    // 16 aisles
                    if (num_aisle == 16) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 4.0 / 16.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 4.0 / 16.0 + (r_loc - a_ratio) / b_ratio * 4.0 / 16.0;
                        } else
                            r_loc = 8.0 / 16.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 8.0 / 16.0;
                    }

                    // 18 aisles
                    if (num_aisle == 18) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 4.0 / 18.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 4.0 / 18.0 + (r_loc - a_ratio) / b_ratio * 4.0 / 18.0;
                        } else
                            r_loc = 8.0 / 18.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 10.0 / 18.0;
                    }

                    // 20 aisles
                    if (num_aisle == 20) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 4.0 / 20.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 4.0 / 20.0 + (r_loc - a_ratio) / b_ratio * 4.0 / 20.0;
                        } else
                            r_loc = 8.0 / 20.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 12.0 / 20.0;
                    }

                    // 22 aisles
                    if (num_aisle == 22) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 4.0 / 22.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 4.0 / 22.0 + (r_loc - a_ratio) / b_ratio * 6.0 / 22.0;
                        } else
                            r_loc = 10.0 / 22.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 12.0 / 22.0;
                    }

                    // 24 aisles
                    if (num_aisle == 24) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 6.0 / 24.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 6.0 / 24.0 + (r_loc - a_ratio) / b_ratio * 6.0 / 24.0;
                        } else
                            r_loc = 12.0 / 24.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 12.0 / 24.0;
                    }

                    // 28 aisles
                    if (num_aisle == 28) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 6.0 / 28.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 6.0 / 28.0 + (r_loc - a_ratio) / b_ratio * 6.0 / 28.0;
                        } else
                            r_loc = 12.0 / 28.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 16.0 / 28.0;
                    }

                    // 30 aisles
                    if (num_aisle == 30) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 6.0 / 30.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 6.0 / 30.0 + (r_loc - a_ratio) / b_ratio * 6.0 / 30.0;
                        } else
                            r_loc = 12.0 / 30.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 18.0 / 30.0;
                    }

                    // 32 aisles
                    if (num_aisle == 32) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 8.0 / 32.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 8.0 / 32.0 + (r_loc - a_ratio) / b_ratio * 8.0 / 32.0;
                        } else
                            r_loc = 16.0 / 32.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 16.0 / 32.0;
                    }

                    // 40 aisles
                    if (num_aisle == 40) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 8.0 / 40.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 8.0 / 40.0 + (r_loc - a_ratio) / b_ratio * 8.0 / 40.0;
                        } else
                            r_loc = 16.0 / 40.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 24.0 / 40.0;
                    }

                    if (str_mode == 1)
                        loc_pickface = (int) (r_loc * (double) num_pickface);
                    else {
                        loc_pickface = (int) (r_loc * 10.0 * (double) num_aisle);
                        loc_pickface = loc_pickface * (aisle_size / 10);
                    }

                } else if (str_mode == 2) {
                    //r_loc = 0.25 * 0.7;

                    //printf(" %f ", r_loc);
                    for (k = 0; k < num_aisle; k++)
                        if (((double) k / (double) num_aisle) <= r_loc)
                            cur_aisle = k;

                    r_loc = (r_loc - (double) cur_aisle / (double) num_aisle) * (double) num_aisle;

                    if (cur_aisle % 2 == 1)
                        r_loc = (1.0 - r_loc);


                    //printf(" %d %f ",cur_aisle, r_loc);
                    // 4 aisles


                    if (num_aisle == 4) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 1.0 / 4.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 1.0 / 4.0 + (r_loc - a_ratio) / b_ratio * 1.0 / 4.0;
                        } else
                            r_loc = 1.0 / 2.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 1.0 / 2.0;
                    }
                    // 6 aisles
                    if (num_aisle == 6) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 1.0 / 6.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 1.0 / 6.0 + (r_loc - a_ratio) / b_ratio * 1.0 / 3.0;
                        } else
                            r_loc = 1.0 / 2.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 1.0 / 2.0;
                    }

                    // 10 aisles
                    if (num_aisle == 10) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 2.0 / 10.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 2.0 / 10.0 + (r_loc - a_ratio) / b_ratio * 3.0 / 10.0;
                        } else
                            r_loc = 5.0 / 10.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 5.0 / 10.0;
                    }

                    if (cur_aisle % 2 == 0)
                        r_loc = (double) cur_aisle / (double) num_aisle + r_loc / (double) num_aisle;
                    else
                        r_loc = (double) cur_aisle / (double) num_aisle + (1.0 - r_loc) / (double) num_aisle;

                    //printf(" %f \n", r_loc);
                    loc_pickface = (int) (r_loc * (double) num_pickface);
                } else if (str_mode == 3) {
                    // 4 aisles
                    if (num_aisle == 4) {
                        if (r_loc < a_ratio + b_ratio) {
                            r_loc = r_loc / (a_ratio + b_ratio) * 1.0 / 2.0;
                        } else
                            r_loc = 1.0 / 2.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 1.0 / 2.0;
                    }
                    // 6 aisles
                    if (num_aisle == 6) {
                        if (r_loc < a_ratio + b_ratio / 2.0) {
                            r_loc = r_loc / (a_ratio + b_ratio / 2.0) * 1.0 / 3.0;
                        } else if (r_loc < a_ratio + b_ratio + c_ratio / 3.0) {
                            r_loc = 1.0 / 3.0 + (r_loc - a_ratio - b_ratio / 2.0) / (b_ratio + c_ratio / 3.0) * 1.0 / 3.0;
                        } else
                            r_loc = 2.0 / 3.0 + (r_loc - a_ratio - b_ratio - c_ratio / 3.0) / (c_ratio * 2.0 / 3.0) * 1.0 / 3.0;
                    }

                    // 10 aisles
                    if (num_aisle == 10) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 2.0 / 10.0;
                        } else if (r_loc < a_ratio + b_ratio + c_ratio / 5.0) {
                            r_loc = 4.0 / 10.0 + (r_loc - a_ratio) / (b_ratio + c_ratio / 5.0) * 4.0 / 10.0;
                        } else
                            r_loc = 6.0 / 10.0 + (r_loc - a_ratio - b_ratio - c_ratio / 5.0) / (c_ratio * 4.0 / 5.0) * 4.0 / 10.0;
                    }

                    loc_pickface = (int) (r_loc * (double) num_pickface);
                } else if (str_mode == 4) {
                    // 4 aisles
                    if (num_aisle == 4) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / (a_ratio) * 1.0 / 2.0;
                        } else
                            r_loc = 1.0 / 2.0 + (r_loc - a_ratio) / (1.0 - a_ratio) * 1.0 / 2.0;
                    }
                    // the first item or random cor

                    num_sector = 4;
                    sector_size = 2 * aisle_size / num_sector;
                    cur_zone = cur_aisle / 2;
                    if (i == 0) {
                        // the first item or random cor
                        loc_pickface = (int) r_loc * num_pickface;

                        cur_aisle = loc_pickface / aisle_size;
                        cur_zone = cur_aisle / 2;

                        num_sector = 4;
                        sector_size = 2 * aisle_size / num_sector;
                        cur_sector = loc_pickface / sector_size - cur_zone * sector_size;
                        cur_depth = loc_pickface % sector_size;
                    } else {
                        // the first item or random cor
                        loc_pickface = (int) r_loc * num_pickface;

                        sel_zone = loc_pickface / (aisle_size * 2);
                        sel_sector = loc_pickface / sector_size - sel_zone * sector_size;
                        sel_depth = loc_pickface % sector_size;  // here the depth is th sector depth, not aisle depth


                        if (cur_zone == sel_zone) {
                            num_sector = 4;
                            sector_size = 2 * aisle_size / num_sector;
                            cur_sector = loc_pickface / sector_size - cur_zone * sector_size;
                            // handle a zone
                            r_loc = (r_loc - 1.0 / 2.0 * (double) cur_zone) * 2.0;

                            if (cor_mode == 0)   // identical sector
                            {
                                if (r_loc < cost_cor) {
                                    rel_pickface = (int) (r_loc / cost_cor * sector_size);

                                    sel_sector = cur_sector;
                                    sel_depth = rel_pickface;

                                } else {
                                    rel_pickface = (int) ((r_loc - cost_cor) / (1.0 - cost_cor) * sector_size * (num_sector - 1));

                                    sel_sector = (int) rel_pickface / sector_size;
                                    sel_depth = (int) rel_pickface % sector_size;

                                    if (sel_sector >= cur_sector)
                                        sel_sector++;
                                }

                            } else if (cor_mode == 1)   // the vetical sector
                            {
                                if (r_loc < cost_cor) {
                                    rel_pickface = (int) (r_loc / cost_cor * sector_size);


                                    if (cur_sector % 2 == 0) // 0 or 2
                                        sel_sector = cur_sector + 1;
                                    else                // 1 or 3
                                        sel_sector = cur_sector - 1;

                                    sel_depth = rel_pickface;

                                } else {
                                    rel_pickface = (int) ((r_loc - cost_cor) / (1.0 - cost_cor) * sector_size * (num_sector - 1));

                                    sel_sector = (int) rel_pickface / sector_size;
                                    sel_depth = (int) rel_pickface % sector_size;

                                    if (cur_sector % 2 == 0) {
                                        if (sel_sector >= (cur_sector + 1))  // if 0 or 2, 1 and 3
                                            sel_sector++;
                                    } else {
                                        if (sel_sector >= (cur_sector - 1))  // if 1 or 3, 0 and 2
                                            sel_sector++;
                                    }
                                }

                            } else if (cor_mode == 2)   // the dianonal sector
                            {
                                if (r_loc < cost_cor) {
                                    rel_pickface = (int) (r_loc / cost_cor * sector_size);


                                    if (cur_sector / 2 == 0)            // 0 or 1
                                        sel_sector = cur_sector + 2;
                                    else                        // 2 or 3
                                        sel_sector = cur_sector - 2;

                                    sel_depth = rel_pickface;

                                } else {
                                    rel_pickface = (int) ((r_loc - cost_cor) / (1.0 - cost_cor) * sector_size * (num_sector - 1));

                                    sel_sector = (int) rel_pickface / sector_size;
                                    sel_depth = (int) rel_pickface % sector_size;

                                    if (cur_sector / 2 == 0) {
                                        if (sel_sector >= (cur_sector + 2))
                                            sel_sector++;
                                    } else {
                                        if (sel_sector >= (cur_sector - 2))
                                            sel_sector++;
                                    }
                                }

                            } else if (cor_mode == 3)   // horizontal
                            {
                                if (r_loc < cost_cor) {
                                    rel_pickface = (int) (r_loc / cost_cor * sector_size);

								/*
								switch(cur_sector)
								{
									case 0: sec_sector=3; break;
									case 1: sec_sector=2; break;
									case 2: sec_sector=1; break;
									case 3: sec_sector=0; break;
								}
								*/

                                    sel_sector = num_sector - 1 - cur_sector;
                                    sel_depth = rel_pickface;

                                } else {
                                    rel_pickface = (int) ((r_loc - cost_cor) / (1.0 - cost_cor) * sector_size * (num_sector - 1));

                                    sel_sector = (int) rel_pickface / sector_size;
                                    sel_depth = (int) rel_pickface % sector_size;

                                    if (sel_sector >= num_sector - 1 - cur_sector)
                                        sel_sector++;
                                }
                            }
                            loc_pickface = (int) (sel_sector * sector_size + sel_depth) + sel_zone * aisle_size * 2;

                        }

                    }
                } else if (str_mode == 5) {
                    // 4 aisles
                    if (num_aisle == 4) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 1.0 / 4.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 1.0 / 4.0 + (r_loc - a_ratio) / b_ratio * 1.0 / 4.0;
                        } else
                            r_loc = 1.0 / 2.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 1.0 / 2.0;
                    }
                    // 6 aisles
                    if (num_aisle == 6) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 1.0 / 6.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 1.0 / 6.0 + (r_loc - a_ratio) / b_ratio * 1.0 / 3.0;
                        } else
                            r_loc = 1.0 / 2.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 1.0 / 2.0;
                    }

                    // 8 aisles
                    if (num_aisle == 8) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 2.0 / 8.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 2.0 / 8.0 + (r_loc - a_ratio) / b_ratio * 2.0 / 8.0;
                        } else
                            r_loc = 4.0 / 8.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 4.0 / 8.0;
                    }

                    // 10 aisles
                    if (num_aisle == 10) {
                        if (r_loc < a_ratio) {
                            r_loc = r_loc / a_ratio * 2.0 / 10.0;
                        } else if (r_loc < a_ratio + b_ratio) {
                            r_loc = 2.0 / 10.0 + (r_loc - a_ratio) / b_ratio * 3.0 / 10.0;
                        } else
                            r_loc = 5.0 / 10.0 + (r_loc - a_ratio - b_ratio) / c_ratio * 5.0 / 10.0;
                    }

                    loc_pickface = (int) (r_loc * (double) num_pickface);
                    loc_pickface = 5 * (int) (loc_pickface / 5);
                } else if (str_mode == 6) {
                    if (num_aisle == 6) {
                        if (o == 0) {
                            // first zone
                            if (i < 7)
                                r_loc = r_loc * 2.0 / 6.0;
                            else
                                r_loc = 2.0 / 6.0 + r_loc * 1.0 / 6.0;

                        } else if (o == 1) {
                            // second zone
                            if (i < 1) {
                                r_loc = 2.0 / 6.0 + r_loc * 1.0 / 6.0;
                            } else if (i < 3) {
                                r_loc = 3.0 / 6.0 + r_loc * 1.0 / 6.0;
                            } else {
                                r_loc = 4.0 / 6.0 + r_loc * 1.0 / 6.0;
                            }

                        } else {
                            // first zone
                            if (r_loc < 2)
                                r_loc = 2.0 / 6.0 + r_loc * 1.0 / 6.0;
                            else
                                r_loc = 3.0 / 6.0 + r_loc * 1.0 / 6.0;
                        }
                    }

                    loc_pickface = (int) (r_loc * (double) num_pickface);
                }
                if (o >= dry_run) {
                    orders[o - dry_run][loc_pickface]++;
                    order_aisle[o - dry_run][(int) loc_pickface / aisle_size] = 1;
                }
            }
        }
        System.out.println("");

        for (int n = 0; n< num_order; n++) {
            Order newOrder = new Order(n, order_aisle[n], orders[n]);
            orderList.add(newOrder);
        }
//        int numOrder, int numAisle, int [][]Vrda, int []capa
        numOrder = num_order;
        numAisle = num_aisle;
        Vrda = order_aisle;
        capa = new int[num_order];

        int count;
        for (i = 0;i<num_order;i++) {
            count = 0;
            for (int j=0;j<num_pickface;j++) {
                if (orders[i][j] != 0) {
                    count++;
                }
                capa[i] = count;
                orderList.get(i).setCapa(count);
            }
        }
        System.out.println("");
        return new RandomData(orderList, numOrder, numAisle, Vrda, capa);
    }
}
